package org.example.beassignment.chat.dto

data class ThreadHistoryPageResponse(
    val threads: List<ThreadHistoryResponse>,
    val page: Int,
    val size: Int,
    val totalCount: Long,
    val totalPages: Int,
)
