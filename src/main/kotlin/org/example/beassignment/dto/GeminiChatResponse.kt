package org.example.beassignment.dto

data class GeminiChatResponse(
    val candidates: List<GeminiCandidate>,
)

data class GeminiCandidate(
    val content: GeminiContent,
    val finishReason: String? = null,
)
