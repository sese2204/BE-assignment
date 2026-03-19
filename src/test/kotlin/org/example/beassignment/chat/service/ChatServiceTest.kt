package org.example.beassignment.chat.service

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.example.beassignment.auth.service.JwtClaims
import org.example.beassignment.chat.builder.SystemPromptBuilder
import org.example.beassignment.chat.client.AiChatClient
import org.example.beassignment.chat.entity.Chat
import org.example.beassignment.chat.entity.Thread
import org.example.beassignment.chat.repository.ChatRepository
import org.example.beassignment.chat.repository.ThreadRepository
import org.example.beassignment.common.BusinessException
import org.example.beassignment.common.ErrorCode
import org.example.beassignment.dto.ChatRequest
import org.example.beassignment.user.entity.User
import org.example.beassignment.user.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional

class ChatServiceTest {

    private val aiChatClient = mockk<AiChatClient>()
    private val threadService = mockk<ThreadService>()
    private val threadRepository = mockk<ThreadRepository>()
    private val chatRepository = mockk<ChatRepository>()
    private val userRepository = mockk<UserRepository>()
    private val systemPromptBuilder = mockk<SystemPromptBuilder>()

    private val chatService = ChatService(
        aiChatClient, threadService, threadRepository, chatRepository, userRepository, systemPromptBuilder,
    )

    private val user = User(id = 1L, email = "test@test.com", passwordHash = "h", name = "Test")
    private val memberClaims = JwtClaims(userId = 1L, role = "member")
    private val adminClaims = JwtClaims(userId = 99L, role = "admin")

    @Test
    fun `chat sends message and returns response`() = runTest {
        val thread = Thread(id = 1L, user = user)
        val request = ChatRequest(message = "hello")

        every { userRepository.findById(1L) } returns Optional.of(user)
        every { threadService.resolveActiveThread(user) } returns thread
        every { systemPromptBuilder.build(any()) } returns "system prompt"
        coEvery { aiChatClient.chat(any(), any()) } returns "AI reply"
        every { chatRepository.save(any()) } returns Chat(
            id = 10L, thread = thread, question = "hello", answer = "AI reply",
        )

        val result = chatService.chat(request, memberClaims)

        assertEquals("AI reply", result.reply)
        assertEquals(1L, result.threadId)
        assertEquals(10L, result.chatId)
    }

    @Test
    fun `chat throws RESOURCE_NOT_FOUND for non-existent user`() = runTest {
        every { userRepository.findById(1L) } returns Optional.empty()

        val ex = assertThrows<BusinessException> { chatService.chat(ChatRequest(message = "hi"), memberClaims) }
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.errorCode)
    }

    @Test
    fun `deleteThread succeeds for owner`() {
        val thread = Thread(id = 1L, user = user)

        every { threadRepository.findById(1L) } returns Optional.of(thread)
        every { threadRepository.delete(thread) } returns Unit

        chatService.deleteThread(1L, memberClaims)

        verify { threadRepository.delete(thread) }
    }

    @Test
    fun `deleteThread succeeds for admin on any thread`() {
        val otherUser = User(id = 2L, email = "other@test.com", passwordHash = "h", name = "Other")
        val thread = Thread(id = 1L, user = otherUser)

        every { threadRepository.findById(1L) } returns Optional.of(thread)
        every { threadRepository.delete(thread) } returns Unit

        chatService.deleteThread(1L, adminClaims)

        verify { threadRepository.delete(thread) }
    }

    @Test
    fun `deleteThread throws FORBIDDEN for non-owner member`() {
        val otherUser = User(id = 2L, email = "other@test.com", passwordHash = "h", name = "Other")
        val thread = Thread(id = 1L, user = otherUser)

        every { threadRepository.findById(1L) } returns Optional.of(thread)

        val ex = assertThrows<BusinessException> { chatService.deleteThread(1L, memberClaims) }
        assertEquals(ErrorCode.FORBIDDEN, ex.errorCode)
    }

    @Test
    fun `deleteThread throws RESOURCE_NOT_FOUND for missing thread`() {
        every { threadRepository.findById(999L) } returns Optional.empty()

        val ex = assertThrows<BusinessException> { chatService.deleteThread(999L, memberClaims) }
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.errorCode)
    }
}
