package org.example.beassignment.dto

import java.time.Instant

data class ChatResponse(
    val chatId: Long,
    val threadId: Long,
    val reply: String,
    val createdAt: Instant,
)
