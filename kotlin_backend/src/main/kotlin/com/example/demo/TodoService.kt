package com.example.demo

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.flipkart.zjsonpatch.JsonDiff
import org.springframework.stereotype.Service
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession

@Service
class TodoService {
    private final val mapper = jacksonObjectMapper()
    private final val todoInstance = Todo()
    private final val sessions: MutableMap<String, WebSocketSession> = mutableMapOf()

    fun applyAction(action: Action) {
        // Grab a mutable reference
        // Serialize out the existing JSON for diffing later on
        val existingState = mapper.valueToTree<JsonNode>(this.todoInstance)

        // apply changes
        todoInstance.apply(action)

        // Serialize out the new JSON for diffing
        val newState = mapper.valueToTree<JsonNode>(this.todoInstance)

        // Get the changes using the `JsonDiff.asJson` method
        val ops = JsonDiff.asJson(existingState, newState)

        println("New Patches: $ops")

        if (ops.isEmpty) {
            return
        }

        for (session in sessions) {
            session.value.sendMessage(
                TextMessage(
                    mapper.writeValueAsString(
                        PatchServerMessage(ops)
                    )
                )
            )
        }
    }

    fun addSession(session: WebSocketSession) {
        val response = mapper.writeValueAsString(
            FullServerMessage(todoInstance)
        )

        println("Sending response message: $response \nto session[${session.id}]")
        session.sendMessage(TextMessage(response))

        sessions[session.id] = session
    }
}