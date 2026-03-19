package org.example.beassignment.chat.service

import org.example.beassignment.auth.service.JwtClaims
import org.example.beassignment.chat.builder.SystemPromptBuilder
import org.example.beassignment.chat.client.AiChatClient
import org.example.beassignment.chat.dto.ChatHistoryItem
import org.example.beassignment.chat.dto.ConversationMessage
import org.example.beassignment.chat.dto.ThreadHistoryPageResponse
import org.example.beassignment.chat.dto.ThreadHistoryResponse
import org.example.beassignment.chat.entity.Chat
import org.example.beassignment.chat.repository.ChatRepository
import org.example.beassignment.chat.repository.ThreadRepository
import org.example.beassignment.common.BusinessException
import org.example.beassignment.common.ErrorCode
import org.example.beassignment.dto.ChatRequest
import org.example.beassignment.dto.ChatResponse
import org.example.beassignment.user.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service("chatServiceV2")
class ChatService(
    private val aiChatClient: AiChatClient,
    private val threadService: ThreadService,
    private val threadRepository: ThreadRepository,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val systemPromptBuilder: SystemPromptBuilder,
) {
    @Transactional
    suspend fun chat(request: ChatRequest, claims: JwtClaims): ChatResponse {
        val user = userRepository.findById(claims.userId).orElseThrow {
            BusinessException(ErrorCode.RESOURCE_NOT_FOUND)
        }
        val thread = threadService.resolveActiveThread(user)

        val history = thread.chats.sortedBy { it.createdAt }.flatMap { c ->
            listOf(
                ConversationMessage(role = "user", content = c.question),
                ConversationMessage(role = "assistant", content = c.answer),
            )
        } + ConversationMessage(role = "user", content = request.message)

        val systemPrompt = systemPromptBuilder.build()
        val messages = if (systemPrompt.isNotBlank()) history else history

        val reply = aiChatClient.chat(messages)

        val chat = chatRepository.save(
            Chat(thread = thread, question = request.message, answer = reply),
        )
        return ChatResponse(
            chatId = chat.id,
            threadId = thread.id,
            reply = reply,
            createdAt = chat.createdAt,
        )
    }

    @Transactional(readOnly = true)
    fun getHistory(
        claims: JwtClaims,
        page: Int,
        size: Int,
        sort: String,
    ): ThreadHistoryPageResponse {
        val direction = if (sort.lowercase() == "asc") Sort.Direction.ASC else Sort.Direction.DESC
        val pageable = PageRequest.of(page - 1, size, Sort.by(direction, "createdAt"))

        val threadPage = if (claims.role == "admin") {
            threadRepository.findAllWithChats(pageable)
        } else {
            threadRepository.findByUserIdWithChats(claims.userId, pageable)
        }

        val threads = threadPage.content.map { thread ->
            val sortedChats = if (direction == Sort.Direction.ASC) {
                thread.chats.sortedBy { it.createdAt }
            } else {
                thread.chats.sortedByDescending { it.createdAt }
            }
            ThreadHistoryResponse(
                threadId = thread.id,
                createdAt = thread.createdAt,
                chats = sortedChats.map { chat ->
                    ChatHistoryItem(
                        chatId = chat.id,
                        question = chat.question,
                        answer = chat.answer,
                        createdAt = chat.createdAt,
                    )
                },
            )
        }

        return ThreadHistoryPageResponse(
            threads = threads,
            page = page,
            size = size,
            totalCount = threadPage.totalElements,
            totalPages = threadPage.totalPages,
        )
    }

    @Transactional
    fun deleteThread(threadId: Long, claims: JwtClaims) {
        val thread = threadRepository.findById(threadId).orElseThrow {
            BusinessException(ErrorCode.RESOURCE_NOT_FOUND)
        }
        if (claims.role != "admin" && thread.user.id != claims.userId) {
            throw BusinessException(ErrorCode.FORBIDDEN)
        }
        threadRepository.delete(thread)
    }
}
