package org.example.beassignment.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "피드백 목록 페이징 응답")
data class FeedbackPageResponse(
    @Schema(description = "피드백 목록")
    val content: List<FeedbackResponse>,
    @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
    val page: Int,
    @Schema(description = "페이지 크기", example = "20")
    val size: Int,
    @Schema(description = "전체 피드백 수", example = "42")
    val totalElements: Long,
    @Schema(description = "전체 페이지 수", example = "3")
    val totalPages: Int,
)
