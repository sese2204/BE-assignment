package org.example.beassignment.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.example.beassignment.entity.Chat
import org.example.beassignment.entity.Thread
import org.example.beassignment.entity.User
import org.example.beassignment.repository.ActivityLogRepository
import org.example.beassignment.repository.ChatRepository
import org.example.beassignment.repository.ThreadRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class ThreadServiceTest {

    private val threadRepository = mockk<ThreadRepository>()
    private val chatRepository = mockk<ChatRepository>()
    private val activityLogRepository = mockk<ActivityLogRepository>()
    private val threadService = ThreadService(threadRepository, chatRepository, activityLogRepository)

    private val user = User(id = 1L, email = "test@test.com", passwordHash = "h", name = "Test")

    @Test
    fun `creates new thread when user has no prior chats`() {
        val newThread = Thread(id = 1L, user = user)

        every { chatRepository.findTopByThreadUserIdOrderByCreatedAtDesc(1L) } returns null
        every { threadRepository.save(any()) } returns newThread
        every { activityLogRepository.save(any()) } returns mockk()

        val result = threadService.resolveActiveThread(user)

        assertEquals(1L, result.id)
        verify { threadRepository.save(any()) }
    }

    @Test
    fun `reuses existing thread when last chat is within 30 minutes`() {
        val existingThread = Thread(id = 10L, user = user)
        val recentChat = Chat(
            id = 1L,
            thread = existingThread,
            question = "hi",
            answer = "hello",
            createdAt = Instant.now().minus(10, ChronoUnit.MINUTES),
        )

        every { chatRepository.findTopByThreadUserIdOrderByCreatedAtDesc(1L) } returns recentChat

        val result = threadService.resolveActiveThread(user)

        assertEquals(10L, result.id)
        verify(exactly = 0) { threadRepository.save(any()) }
    }

    @Test
    fun `creates new thread when last chat is over 30 minutes ago`() {
        val oldThread = Thread(id = 10L, user = user)
        val oldChat = Chat(
            id = 1L,
            thread = oldThread,
            question = "hi",
            answer = "hello",
            createdAt = Instant.now().minus(60, ChronoUnit.MINUTES),
        )
        val newThread = Thread(id = 20L, user = user)

        every { chatRepository.findTopByThreadUserIdOrderByCreatedAtDesc(1L) } returns oldChat
        every { threadRepository.save(any()) } returns newThread
        every { activityLogRepository.save(any()) } returns mockk()

        val result = threadService.resolveActiveThread(user)

        assertEquals(20L, result.id)
        verify { threadRepository.save(any()) }
    }
}
