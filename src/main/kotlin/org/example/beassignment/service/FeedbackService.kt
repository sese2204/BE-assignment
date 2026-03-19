package org.example.beassignment.service

import org.example.beassignment.common.BusinessException
import org.example.beassignment.common.ErrorCode
import org.example.beassignment.dto.CreateFeedbackRequest
import org.example.beassignment.dto.FeedbackPageResponse
import org.example.beassignment.dto.FeedbackResponse
import org.example.beassignment.dto.UpdateFeedbackStatusRequest
import org.example.beassignment.entity.Feedback
import org.example.beassignment.entity.FeedbackStatus
import org.springframework.data.domain.Pageable
import org.example.beassignment.repository.FeedbackRepository
import org.example.beassignment.repository.ThreadRepository
import org.example.beassignment.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FeedbackService(
    private val feedbackRepository: FeedbackRepository,
    private val threadRepository: ThreadRepository,
    private val userRepository: UserRepository,
) {

    @Transactional
    fun createFeedback(userId: Long, role: String, request: CreateFeedbackRequest): FeedbackResponse {
        val thread = threadRepository.findById(request.threadId)
            .orElseThrow { BusinessException(ErrorCode.RESOURCE_NOT_FOUND) }

        if (role != "admin" && thread.user.id != userId) {
            throw BusinessException(ErrorCode.FORBIDDEN)
        }

        if (feedbackRepository.existsByUserIdAndThreadId(userId, request.threadId)) {
            throw BusinessException(ErrorCode.DUPLICATE_FEEDBACK)
        }

        val user = if (thread.user.id == userId) {
            thread.user
        } else {
            userRepository.findById(userId)
                .orElseThrow { BusinessException(ErrorCode.RESOURCE_NOT_FOUND) }
        }

        val feedback = feedbackRepository.save(
            Feedback(
                user = user,
                thread = thread,
                isPositive = request.isPositive,
            ),
        )

        return FeedbackResponse.from(feedback)
    }

    @Transactional(readOnly = true)
    fun listFeedback(userId: Long, role: String, isPositive: Boolean?, pageable: Pageable): FeedbackPageResponse {
        val page = if (role == "admin") {
            if (isPositive != null) {
                feedbackRepository.findByIsPositive(isPositive, pageable)
            } else {
                feedbackRepository.findAll(pageable)
            }
        } else {
            if (isPositive != null) {
                feedbackRepository.findByUserIdAndIsPositive(userId, isPositive, pageable)
            } else {
                feedbackRepository.findByUserId(userId, pageable)
            }
        }

        return FeedbackPageResponse(
            content = page.content.map { FeedbackResponse.from(it) },
            page = page.number,
            size = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
        )
    }

    @Transactional
    fun updateStatus(feedbackId: Long, request: UpdateFeedbackStatusRequest): FeedbackResponse {
        val feedback = feedbackRepository.findById(feedbackId)
            .orElseThrow { BusinessException(ErrorCode.RESOURCE_NOT_FOUND) }

        val newStatus = try {
            FeedbackStatus.valueOf(request.status.uppercase())
        } catch (e: IllegalArgumentException) {
            throw BusinessException(ErrorCode.INVALID_REQUEST)
        }

        feedback.status = newStatus
        return FeedbackResponse.from(feedbackRepository.save(feedback))
    }
}
