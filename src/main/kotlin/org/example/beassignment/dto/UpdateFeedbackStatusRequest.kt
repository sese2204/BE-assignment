package org.example.beassignment.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "피드백 상태 변경 요청")
data class UpdateFeedbackStatusRequest(
    @Schema(description = "변경할 상태 (PENDING 또는 RESOLVED)", example = "RESOLVED")
    @field:NotBlank
    val status: String,
)
