package org.example.beassignment.chat.service

import org.example.beassignment.chat.entity.Thread
import org.example.beassignment.chat.repository.ChatRepository
import org.example.beassignment.chat.repository.ThreadRepository
import org.example.beassignment.user.entity.User
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class ThreadService(
    private val threadRepository: ThreadRepository,
    private val chatRepository: ChatRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun resolveActiveThread(user: User): Thread {
        val lastChat = chatRepository.findTopByThreadUserIdOrderByCreatedAtDesc(user.id)

        if (lastChat == null) {
            log.debug("No prior chats for userId={}, creating new thread", user.id)
            return threadRepository.save(Thread(user = user))
        }

        val minutesSinceLast = ChronoUnit.MINUTES.between(lastChat.createdAt, Instant.now())
        return if (minutesSinceLast > 30) {
            log.debug("Last chat was {}m ago for userId={}, creating new thread", minutesSinceLast, user.id)
            threadRepository.save(Thread(user = user))
        } else {
            log.debug("Reusing thread id={} for userId={}", lastChat.thread.id, user.id)
            lastChat.thread
        }
    }
}
