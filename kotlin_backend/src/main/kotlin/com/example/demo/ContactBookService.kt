package com.example.demo

import com.example.demo.repository.ContactBookDoc
import com.example.demo.repository.ContactRepository
import com.example.demo.repository.PatchDoc
import com.example.demo.repository.PatchRepository
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.flipkart.zjsonpatch.JsonDiff
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.stereotype.Service
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.optionals.getOrElse

@Service
class ContactBookService(
    private val patchRepository: PatchRepository,
    private val contactRepository: ContactRepository,
) {
    private val mapper = jacksonObjectMapper()

    private val mutex = Mutex()

    private val contactBooks = ConcurrentHashMap<String, ContactBook>()
    private val sessionPool: ConcurrentHashMap<String, MutableList<WebSocketSession>> = ConcurrentHashMap()

    suspend fun registerSession(session: WebSocketSession) {
        val userId = session.getUserId()
        // 1. add session to the Pool
        // 2. re-hydrate contact book of this user
        val contactBook = contactBooks[userId]
            ?: withContext(Dispatchers.IO) {
                contactRepository.findById(userId)
                    .getOrElse {
                        contactRepository.save(ContactBookDoc(id = userId))
                    }
                    .toContactBook()
            }

        contactBooks[userId] = contactBook

        println("Establish new websocket connection ${session.id} for user[$userId]")
        val fullResponse = TextMessage(
            mapper.writeValueAsString(
                FullServerMessage(contactBook)
            )
        )

        runCatching {
            println("Sending Full data: $fullResponse \nto new session[${session.id}] for user[$userId]")
            session.sendMessage(fullResponse)
        }.onSuccess {
            println("Send Full successful. Adding session to our list")

            if (!sessionPool.containsKey(userId)) {
                sessionPool[userId] = Collections.synchronizedList(mutableListOf(session))
            }
            sessionPool[userId]?.add(session)
        }.onFailure {
            println("Could not send initial state update because of error: $it")
        }
    }

    fun removeSession(session: WebSocketSession) {
        val userId = session.getUserId()
        println("Removing session[${session.id}] from session list for user[$userId]")

        sessionPool[userId]?.removeIf { it.id == session.id }
    }

    suspend fun applyAction(action: Action, session: WebSocketSession) {
        val userId = session.getUserId()

        // Grab a mutable reference
        // Serialize out the existing JSON for diffing later on
        val contactBook = contactBooks[userId]!!
        val nowState = mapper.valueToTree<JsonNode>(contactBook)

        // apply changes
        contactBook.apply(action)

        // Serialize out the new JSON for diffing
        val nextState = mapper.valueToTree<JsonNode>(contactBook)

        // Get the changes using the `JsonDiff.asJson` method
        val patch = JsonDiff.asJson(nowState, nextState)

        println("New patch created: $patch")

        if (patch.isEmpty) {
            return
        }

        coroutineScope {
            launch {
                patchRepository.save(PatchDoc(userId = userId, ops = patch))
                println("persisted patch into physical storage for user[$userId]")
            }
            launch {
                contactRepository.save(ContactBookDoc.from(contactBook))
                println("persisted contacts into physical storage for user[$userId]")
            }
            launch {
                println("broadcasting patches to live ws sessions for user[$userId]")
                val patchResponse = TextMessage(
                    mapper.writeValueAsString(
                        PatchServerMessage(patch)
                    )
                )
                broadcastMsgToAllSessions(patchResponse, userId)
            }
        }
    }

    private suspend fun broadcastMsgToAllSessions(msg: TextMessage, userId: String) = coroutineScope {
        val userSessions = sessionPool[userId]!!
        for (session in userSessions) {
            launch {
                runCatching {
                    mutex.withLock {
                        if (session.isOpen) {
                            session.sendMessage(msg)
                        } else {
                            delay(50L)
                            session.sendMessage(msg)
                        }
                    }
                }.onSuccess {
                    println("Send Patch to session[${session.id}] successful for user[$userId].")
                }.onFailure {
                    println("Error sending patch to session [${session.id}] for user[$userId]. Removing from session list")
                    println("Error: $it")
                    userSessions.removeIf { it.id == session.id }
                }
            }
        }
    }
}