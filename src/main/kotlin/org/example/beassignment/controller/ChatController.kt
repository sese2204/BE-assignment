package org.example.beassignment.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.example.beassignment.common.ApiResponse
import org.example.beassignment.dto.ChatRequest
import org.example.beassignment.dto.ChatResponse
import org.example.beassignment.dto.ThreadHistoryPageResponse
import org.example.beassignment.service.ChatService
import org.example.beassignment.service.JwtClaims
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Chat", description = "AI 채팅 및 대화 관리")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/chat")
class ChatController(
    private val chatService: ChatService,
) {
    @Operation(summary = "AI 채팅 메시지 전송", description = "질문을 전송하고 AI 응답을 받습니다.")
    @PostMapping
    suspend fun chat(
        @Valid @RequestBody request: ChatRequest,
        @AuthenticationPrincipal claims: JwtClaims,
    ): ResponseEntity<ApiResponse<ChatResponse>> {
        val result = chatService.chat(request, claims)
        return ResponseEntity.ok(ApiResponse.ok(result))
    }

    @Operation(summary = "대화 목록 조회", description = "스레드 단위로 그룹화된 대화 목록을 페이지네이션과 정렬을 지원하여 조회합니다.")
    @GetMapping("/history")
    fun getHistory(
        @AuthenticationPrincipal claims: JwtClaims,
        @Parameter(description = "페이지 번호 (1부터 시작)") @RequestParam(defaultValue = "1") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") size: Int,
        @Parameter(description = "정렬 방향: asc 또는 desc") @RequestParam(defaultValue = "desc") sort: String,
    ): ResponseEntity<ApiResponse<ThreadHistoryPageResponse>> {
        val result = chatService.getHistory(claims, page, size, sort)
        return ResponseEntity.ok(ApiResponse.ok(result))
    }

    @Operation(summary = "스레드 삭제", description = "특정 스레드와 포함된 모든 대화를 삭제합니다. 멤버는 자신의 스레드만, 관리자는 모든 스레드를 삭제할 수 있습니다.")
    @DeleteMapping("/threads/{threadId}")
    fun deleteThread(
        @PathVariable threadId: Long,
        @AuthenticationPrincipal claims: JwtClaims,
    ): ResponseEntity<ApiResponse<Unit>> {
        chatService.deleteThread(threadId, claims)
        return ResponseEntity.ok(ApiResponse.ok(Unit))
    }
}
