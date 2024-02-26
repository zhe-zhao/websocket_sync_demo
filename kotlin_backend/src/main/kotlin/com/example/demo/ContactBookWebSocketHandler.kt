package com.example.demo

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Component
class ContactBookWebSocketHandler(
    val contactBookService: ContactBookService
) : TextWebSocketHandler() {
    private val mapper = jacksonObjectMapper()

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        println("Received action: ${message.payload}")
        val receivedAction = mapper.readValue(message.payload, Action::class.java)

        GlobalScope.launch {
            contactBookService.applyAction(receivedAction, session)
        }
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        GlobalScope.launch {
            contactBookService.registerSession(session)
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        GlobalScope.launch {
            contactBookService.removeSession(session)
        }
    }

}

fun WebSocketSession.getUserId(): String {
    val pairs = uri!!.query.split("&")

    for (pair in pairs) {
        val index = pair.indexOf("=")
        if (pair.substring(0, index) == "userId") {
            return pair.substring(index + 1)
        }
    }
    throw RuntimeException("user not found")
}
