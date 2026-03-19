package org.example.beassignment.client

import org.example.beassignment.dto.ConversationMessage

interface AiChatClient {

    suspend fun chat(
        messages: List<ConversationMessage>,
        contextChunks: List<String> = emptyList(),
    ): String
}
