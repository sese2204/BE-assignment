package org.example.beassignment.chat.client

import org.example.beassignment.chat.dto.GeminiChatRequest
import org.example.beassignment.chat.dto.GeminiChatResponse
import org.example.beassignment.chat.dto.GeminiContent
import org.example.beassignment.chat.dto.GeminiPart
import org.example.beassignment.chat.dto.GeminiSystemInstruction
import org.example.beassignment.common.BusinessException
import org.example.beassignment.common.ErrorCode
import org.example.beassignment.config.AiProperties
import org.slf4j.LoggerFactory
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException

@Component
class AiApiClient(
    private val aiWebClient: WebClient,
    private val aiProperties: AiProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Retryable(
        retryFor = [WebClientRequestException::class, WebClientResponseException::class],
        noRetryFor = [BusinessException::class],
        maxAttemptsExpression = "\${ai.max-retries:3}",
        backoff = Backoff(delay = 1000L, multiplier = 2.0),
    )
    fun chat(userMessage: String): String {
        val startTime = System.currentTimeMillis()
        val model = aiProperties.modelName

        log.info("Calling Gemini API: model={}", model)

        val request = GeminiChatRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = userMessage)),
                ),
            ),
            systemInstruction = GeminiSystemInstruction(
                parts = listOf(GeminiPart(text = aiProperties.systemPrompt)),
            ),
        )

        val response = aiWebClient.post()
            .uri("/v1beta/models/{model}:generateContent", model)
            .header("x-goog-api-key", aiProperties.apiKey)
            .bodyValue(request)
            .retrieve()
            .onStatus({ it.is4xxClientError }) { clientResponse ->
                log.warn(
                    "Gemini API 4xx error (non-retryable): model={}, status={}",
                    model,
                    clientResponse.statusCode(),
                )
                throw BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE)
            }
            .bodyToMono(GeminiChatResponse::class.java)
            .block()

        val elapsed = System.currentTimeMillis() - startTime
        log.info("Gemini API success: model={}, elapsedMs={}", model, elapsed)

        return response?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE)
    }

    @Recover
    fun recoverFromRequestException(e: WebClientRequestException, userMessage: String): String {
        log.error("Gemini API network error after all retries: model={}, error={}", aiProperties.modelName, e.message)
        throw BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE)
    }

    @Recover
    fun recoverFromResponseException(e: WebClientResponseException, userMessage: String): String {
        log.error(
            "Gemini API server error after all retries: model={}, status={}",
            aiProperties.modelName,
            e.statusCode,
        )
        throw BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE)
    }
}
