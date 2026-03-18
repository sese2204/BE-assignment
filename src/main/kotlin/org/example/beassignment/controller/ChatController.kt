package org.example.beassignment.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.example.beassignment.common.ApiResponse
import org.example.beassignment.dto.ChatRequest
import org.example.beassignment.dto.ChatResponse
import org.example.beassignment.service.ChatService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Chat", description = "AI chat endpoints")
@RestController
@RequestMapping("/api/v1")
class ChatController(
    private val chatService: ChatService,
) {
    @Operation(
        summary = "Send a message to the AI",
        description = "Sends a single user message to Gemini and returns the AI-generated reply.",
        responses = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "AI reply returned successfully",
                content = [Content(schema = Schema(implementation = ChatResponse::class))],
            ),
            SwaggerApiResponse(responseCode = "400", description = "Invalid request (blank or too long message)"),
            SwaggerApiResponse(responseCode = "503", description = "AI service unavailable after retries"),
        ],
    )
    @PostMapping("/chat")
    fun chat(@Valid @RequestBody request: ChatRequest): ResponseEntity<ApiResponse<ChatResponse>> {
        val result = chatService.chat(request)
        return ResponseEntity.ok(ApiResponse.ok(result))
    }
}
