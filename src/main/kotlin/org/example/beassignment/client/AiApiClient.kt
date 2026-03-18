package org.example.beassignment.client

import org.example.beassignment.common.BusinessException
import org.example.beassignment.common.ErrorCode
import org.example.beassignment.common.retryWithBackoff
import org.example.beassignment.config.AiProperties
import org.example.beassignment.dto.GeminiChatRequest
import org.example.beassignment.dto.GeminiChatResponse
import org.example.beassignment.dto.GeminiContent
import org.example.beassignment.dto.GeminiPart
import org.example.beassignment.dto.GeminiSystemInstruction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.core.publisher.Mono

@Component
class AiApiClient(
    private val aiWebClient: WebClient,
    private val aiProperties: AiProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun chat(userMessage: String): String {
        val model = aiProperties.modelName
        val request = buildRequest(userMessage)

        try {
            return retryWithBackoff(
                maxAttempts = aiProperties.maxRetries,
                retryOn = { it !is BusinessException },
            ) {
                val startTime = System.currentTimeMillis()
                log.info("Calling Gemini API: model={}", model)

                val response = aiWebClient.post()
                    .uri("/v1beta/models/{model}:generateContent", model)
                    .header("x-goog-api-key", aiProperties.apiKey)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus({ it.is4xxClientError }) { _ ->
                        log.warn("Gemini API 4xx error (non-retryable): model={}", model)
                        Mono.error(BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE))
                    }
                    .awaitBody<GeminiChatResponse>()

                val elapsed = System.currentTimeMillis() - startTime
                log.info("Gemini API success: model={}, elapsedMs={}", model, elapsed)

                response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: throw BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE)
            }
        } catch (e: BusinessException) {
            throw e
        } catch (e: Exception) {
            log.error("Gemini API error after all retries: model={}, error={}", model, e.message)
            throw BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE)
        }
    }

    private fun buildRequest(userMessage: String) = GeminiChatRequest(
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
}
