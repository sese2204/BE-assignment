package org.example.beassignment.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

@Schema(description = "피드백 생성 요청")
data class CreateFeedbackRequest(
    @Schema(description = "피드백 대상 대화(thread) ID", example = "1")
    @field:NotNull
    val threadId: Long,

    @Schema(description = "긍정 여부 (true: 긍정, false: 부정)", example = "true")
    @field:NotNull
    val isPositive: Boolean,
)
