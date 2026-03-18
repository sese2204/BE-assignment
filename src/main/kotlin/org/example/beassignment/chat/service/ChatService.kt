package org.example.beassignment.chat.service

import org.example.beassignment.chat.client.AiApiClient
import org.example.beassignment.chat.dto.ChatRequest
import org.example.beassignment.chat.dto.ChatResponse
import org.springframework.stereotype.Service

@Service
class ChatService(
    private val aiApiClient: AiApiClient,
) {
    fun chat(request: ChatRequest): ChatResponse {
        val reply = aiApiClient.chat(request.message)
        return ChatResponse(reply = reply)
    }
}
