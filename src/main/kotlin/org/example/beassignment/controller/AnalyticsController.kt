package org.example.beassignment.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.beassignment.common.ApiResponse
import org.example.beassignment.dto.ActivitySummaryResponse
import org.example.beassignment.service.AnalyticsService
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@Tag(name = "Analytics", description = "관리자 전용 분석 및 보고 기능")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
class AnalyticsController(
    private val analyticsService: AnalyticsService,
) {

    @Operation(
        summary = "사용자 활동 요약 조회",
        description = "요청 시점 기준 최근 24시간 동안의 회원가입, 로그인, 대화 생성 횟수를 조회합니다. 관리자만 호출 가능합니다.",
    )
    @GetMapping("/activity-summary")
    fun getActivitySummary(): ApiResponse<ActivitySummaryResponse> {
        return ApiResponse.ok(analyticsService.getActivitySummary())
    }

    @Operation(
        summary = "CSV 보고서 다운로드",
        description = "최근 24시간 동안 생성된 모든 대화 목록을 CSV 파일로 다운로드합니다. 보고서에는 thread_id, user_email, user_name, created_at 컬럼이 포함됩니다. 관리자만 호출 가능합니다.",
    )
    @GetMapping("/report")
    fun generateReport(): ResponseEntity<ByteArrayResource> {
        val csvBytes = analyticsService.generateCsvReport()
        val filename = "conversation-report-${LocalDate.now()}.csv"
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$filename\"")
            .contentType(MediaType.parseMediaType("text/csv"))
            .contentLength(csvBytes.size.toLong())
            .body(ByteArrayResource(csvBytes))
    }
}
