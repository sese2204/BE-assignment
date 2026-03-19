package org.example.beassignment.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.example.beassignment.entity.Feedback
import java.time.Instant

@Schema(description = "피드백 응답")
data class FeedbackResponse(
    @Schema(description = "피드백 ID", example = "1")
    val id: Long,
    @Schema(description = "대화(thread) ID", example = "1")
    val threadId: Long,
    @Schema(description = "작성자 ID", example = "1")
    val userId: Long,
    @Schema(description = "긍정 여부", example = "true")
    val isPositive: Boolean,
    @Schema(description = "피드백 상태 (PENDING, RESOLVED)", example = "PENDING")
    val status: String,
    @Schema(description = "생성일시")
    val createdAt: Instant,
) {
    companion object {
        fun from(feedback: Feedback): FeedbackResponse = FeedbackResponse(
            id = feedback.id,
            threadId = feedback.thread.id,
            userId = feedback.user.id,
            isPositive = feedback.isPositive,
            status = feedback.status.name,
            createdAt = feedback.createdAt,
        )
    }
}
