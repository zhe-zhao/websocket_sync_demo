package com.example.demo

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Component
class TodoWebSocketHandler(
    val todoService: TodoService
) : TextWebSocketHandler() {
    private val mapper = jacksonObjectMapper()

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        println("Received action: ${message.payload}")
        val receivedAction = jacksonObjectMapper().readValue(message.payload, Action::class.java)

        todoService.applyAction(receivedAction)

        val response = FullServerMessage(
            ServerMessage.Type.Full, todoService.getFull()
        )

        println("Sending response message: $response to session[${session.id}")
        session.sendMessage(TextMessage(mapper.writeValueAsString(response)))
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        println("Establish new websocket connection ${session.id}")
        val response = FullServerMessage(
            ServerMessage.Type.Full, todoService.getFull()
        )

        println("Sending response message: $response to session[${session.id}]")
        session.sendMessage(TextMessage(mapper.writeValueAsString(response)))
    }
}
