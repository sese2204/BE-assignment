package org.example.beassignment.chat.dto

import java.time.Instant

data class ThreadHistoryResponse(
    val threadId: Long,
    val createdAt: Instant,
    val chats: List<ChatHistoryItem>,
)
