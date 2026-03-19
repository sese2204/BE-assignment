package org.example.beassignment.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "사용자 활동 요약 응답 (최근 24시간)")
data class ActivitySummaryResponse(
    @Schema(description = "회원가입 수", example = "15")
    val signupCount: Long,
    @Schema(description = "로그인 수", example = "120")
    val loginCount: Long,
    @Schema(description = "대화 생성 수", example = "45")
    val threadCreateCount: Long,
    @Schema(description = "집계 시작 시점")
    val periodStart: Instant,
    @Schema(description = "집계 종료 시점")
    val periodEnd: Instant,
)
