package org.example.beassignment.chat.client

import kotlinx.coroutines.flow.Flow
import org.example.beassignment.chat.dto.ConversationMessage

interface AiChatClient {

    suspend fun chat(
        messages: List<ConversationMessage>,
        contextChunks: List<String> = emptyList(),
    ): String

    fun chatStream(
        messages: List<ConversationMessage>,
        contextChunks: List<String> = emptyList(),
    ): Flow<String>
}
