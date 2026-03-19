package org.example.beassignment.repository

import org.example.beassignment.entity.Chat
import org.springframework.data.jpa.repository.JpaRepository

interface ChatRepository : JpaRepository<Chat, Long> {
    fun findTopByThreadUserIdOrderByCreatedAtDesc(userId: Long): Chat?
}
