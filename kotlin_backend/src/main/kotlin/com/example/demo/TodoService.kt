package com.example.demo

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.flipkart.zjsonpatch.JsonDiff
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.stereotype.Service
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap

@Service
class TodoService {
    private val mapper = jacksonObjectMapper()

    private val mutex = Mutex()

    private val todoInstance = Todo()
    private val sessions = ConcurrentHashMap<String, WebSocketSession>()

    suspend fun addSession(session: WebSocketSession) {
        println("Establish new websocket connection ${session.id}")
        val fullResponse = TextMessage(
            mapper.writeValueAsString(
                FullServerMessage(todoInstance)
            )
        )

        runCatching {
            println("Sending Full data: $fullResponse \nto new session[${session.id}]")
            session.sendMessage(fullResponse)
        }.onSuccess {
            println("Send Full successful. Adding session to our list")
            sessions[session.id] = session
        }.onFailure {
            println("Could not send initial state update because of error: $it")
        }
    }

    suspend fun applyAction(action: Action) {
        // Grab a mutable reference
        // Serialize out the existing JSON for diffing later on
        val existingState = mapper.valueToTree<JsonNode>(this.todoInstance)

        // apply changes
        todoInstance.apply(action)

        // Serialize out the new JSON for diffing
        val newState = mapper.valueToTree<JsonNode>(this.todoInstance)

        // Get the changes using the `JsonDiff.asJson` method
        val patches = JsonDiff.asJson(existingState, newState)

        println("New Patches: $patches")

        if (patches.isEmpty) {
            return
        }

        coroutineScope {
            launch {
                delay(1000L)
                println("persisted patch into physical storage")
            }
            launch {
                println("broadcasting ops to live ws sessions")
                val patchResponse = TextMessage(
                    mapper.writeValueAsString(
                        PatchServerMessage(patches)
                    )
                )
                broadcastMsgToAllSessions(patchResponse)
            }
        }
    }

    private suspend fun broadcastMsgToAllSessions(msg: TextMessage) = coroutineScope {
        for ((key, value) in sessions) {
            launch {
                runCatching {
                    mutex.withLock {
                        if (value.isOpen) {
                            value.sendMessage(msg)
                        } else {
                            delay(50L)
                            value.sendMessage(msg)
                        }
                    }
                }.onSuccess {
                    println("Send Patch to session[${value.id}] successful.")
                }.onFailure {
                    println("Error sending patch to session [${value.id}]. Removing from session list")
                    println("Error: $it")
                    sessions.remove(key)
                }
            }
        }
    }
}