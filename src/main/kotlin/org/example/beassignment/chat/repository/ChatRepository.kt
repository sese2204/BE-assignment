package org.example.beassignment.chat.repository

import org.example.beassignment.chat.entity.Chat
import org.springframework.data.jpa.repository.JpaRepository

interface ChatRepository : JpaRepository<Chat, Long> {
    fun findTopByThreadUserIdOrderByCreatedAtDesc(userId: Long): Chat?
}
