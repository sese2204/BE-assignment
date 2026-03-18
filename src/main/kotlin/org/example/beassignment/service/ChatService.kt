package org.example.beassignment.service

import org.example.beassignment.client.AiApiClient
import org.example.beassignment.dto.ChatRequest
import org.example.beassignment.dto.ChatResponse
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
