package org.example.beassignment.chat.dto

// Gemini API response format
data class GeminiChatResponse(
    val candidates: List<GeminiCandidate>,
)

data class GeminiCandidate(
    val content: GeminiContent,
    val finishReason: String? = null,
)
