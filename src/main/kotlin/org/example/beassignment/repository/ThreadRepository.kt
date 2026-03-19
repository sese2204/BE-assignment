package org.example.beassignment.repository

import org.example.beassignment.entity.Thread
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ThreadRepository : JpaRepository<Thread, Long> {

    @Query(
        value = "SELECT DISTINCT t FROM Thread t LEFT JOIN FETCH t.chats WHERE t.user.id = :userId",
        countQuery = "SELECT COUNT(t) FROM Thread t WHERE t.user.id = :userId",
    )
    fun findByUserIdWithChats(userId: Long, pageable: Pageable): Page<Thread>

    @Query(
        value = "SELECT DISTINCT t FROM Thread t LEFT JOIN FETCH t.chats",
        countQuery = "SELECT COUNT(t) FROM Thread t",
    )
    fun findAllWithChats(pageable: Pageable): Page<Thread>
}
