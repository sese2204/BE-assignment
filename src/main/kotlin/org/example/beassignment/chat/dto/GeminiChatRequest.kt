package org.example.beassignment.chat.dto

// Gemini API request format:
// POST /v1beta/models/{model}:generateContent
data class GeminiChatRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiSystemInstruction? = null,
)

data class GeminiContent(
    val role: String,
    val parts: List<GeminiPart>,
)

data class GeminiPart(
    val text: String,
)

data class GeminiSystemInstruction(
    val parts: List<GeminiPart>,
)
