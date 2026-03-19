package org.example.beassignment.dto

import java.time.Instant

data class ChatHistoryItem(
    val chatId: Long,
    val question: String,
    val answer: String,
    val createdAt: Instant,
)
