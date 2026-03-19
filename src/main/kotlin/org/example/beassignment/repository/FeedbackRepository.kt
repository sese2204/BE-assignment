package org.example.beassignment.repository

import org.example.beassignment.entity.Feedback
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface FeedbackRepository : JpaRepository<Feedback, Long> {

    fun findByUserId(userId: Long, pageable: Pageable): Page<Feedback>

    fun findByUserIdAndIsPositive(userId: Long, isPositive: Boolean, pageable: Pageable): Page<Feedback>

    fun findByIsPositive(isPositive: Boolean, pageable: Pageable): Page<Feedback>

    fun existsByUserIdAndThreadId(userId: Long, threadId: Long): Boolean
}
