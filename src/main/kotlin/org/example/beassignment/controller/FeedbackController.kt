package org.example.beassignment.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.example.beassignment.common.ApiResponse
import org.example.beassignment.dto.CreateFeedbackRequest
import org.example.beassignment.dto.FeedbackPageResponse
import org.example.beassignment.dto.FeedbackResponse
import org.example.beassignment.dto.UpdateFeedbackStatusRequest
import org.example.beassignment.service.FeedbackService
import org.example.beassignment.service.JwtClaims
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Feedback", description = "피드백 생성, 조회, 상태 변경")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/feedbacks")
class FeedbackController(
    private val feedbackService: FeedbackService,
) {

    @Operation(
        summary = "피드백 생성",
        description = "특정 대화(thread)에 대한 피드백을 생성합니다. 일반 사용자는 자신의 대화에만, 관리자는 모든 대화에 피드백을 남길 수 있습니다. 동일 사용자가 같은 대화에 중복 피드백을 생성하면 409 에러가 반환됩니다.",
    )
    @PostMapping
    fun createFeedback(
        @AuthenticationPrincipal claims: JwtClaims,
        @Valid @RequestBody request: CreateFeedbackRequest,
    ): ResponseEntity<ApiResponse<FeedbackResponse>> {
        val response = feedbackService.createFeedback(claims.userId, claims.role, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response))
    }

    @Operation(
        summary = "피드백 목록 조회",
        description = "피드백 목록을 페이지네이션하여 조회합니다. 일반 사용자는 자신의 피드백만, 관리자는 전체 피드백을 조회할 수 있습니다. 긍정/부정 필터링 및 생성일시 기준 정렬을 지원합니다.",
    )
    @GetMapping
    fun listFeedback(
        @AuthenticationPrincipal claims: JwtClaims,
        @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") size: Int,
        @Parameter(description = "정렬 방향: asc 또는 desc (생성일시 기준)") @RequestParam(defaultValue = "desc") sort: String,
        @Parameter(description = "긍정/부정 필터 (true: 긍정, false: 부정, 미입력: 전체)") @RequestParam(required = false) isPositive: Boolean?,
    ): ApiResponse<FeedbackPageResponse> {
        val direction = if (sort == "asc") Sort.Direction.ASC else Sort.Direction.DESC
        val pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"))
        return ApiResponse.ok(feedbackService.listFeedback(claims.userId, claims.role, isPositive, pageable))
    }

    @Operation(
        summary = "피드백 상태 변경 (관리자 전용)",
        description = "피드백의 상태를 PENDING 또는 RESOLVED로 변경합니다. 관리자만 호출 가능하며, 존재하지 않는 피드백 ID는 404를 반환합니다.",
    )
    @PatchMapping("/{feedbackId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateFeedbackStatus(
        @Parameter(description = "피드백 ID") @PathVariable feedbackId: Long,
        @Valid @RequestBody request: UpdateFeedbackStatusRequest,
    ): ApiResponse<FeedbackResponse> {
        return ApiResponse.ok(feedbackService.updateStatus(feedbackId, request))
    }
}
