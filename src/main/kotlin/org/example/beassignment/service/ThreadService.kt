package org.example.beassignment.service

import org.example.beassignment.entity.ActivityLog
import org.example.beassignment.entity.EventType
import org.example.beassignment.entity.Thread
import org.example.beassignment.entity.User
import org.example.beassignment.repository.ActivityLogRepository
import org.example.beassignment.repository.ChatRepository
import org.example.beassignment.repository.ThreadRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class ThreadService(
    private val threadRepository: ThreadRepository,
    private val chatRepository: ChatRepository,
    private val activityLogRepository: ActivityLogRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun resolveActiveThread(user: User): Thread {
        val lastChat = chatRepository.findTopByThreadUserIdOrderByCreatedAtDesc(user.id)

        if (lastChat == null) {
            log.debug("No prior chats for userId={}, creating new thread", user.id)
            val thread = threadRepository.save(Thread(user = user))
            activityLogRepository.save(ActivityLog(user = user, eventType = EventType.THREAD_CREATE))
            return thread
        }

        val minutesSinceLast = ChronoUnit.MINUTES.between(lastChat.createdAt, Instant.now())
        return if (minutesSinceLast > 30) {
            log.debug("Last chat was {}m ago for userId={}, creating new thread", minutesSinceLast, user.id)
            val thread = threadRepository.save(Thread(user = user))
            activityLogRepository.save(ActivityLog(user = user, eventType = EventType.THREAD_CREATE))
            thread
        } else {
            log.debug("Reusing thread id={} for userId={}", lastChat.thread.id, user.id)
            lastChat.thread
        }
    }
}
