package org.example.beassignment.client

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.example.beassignment.chat.builder.SystemPromptBuilder
import org.example.beassignment.chat.client.AiChatClient
import org.example.beassignment.chat.dto.ConversationMessage
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
    private val systemPromptBuilder: SystemPromptBuilder,
) : AiChatClient {

    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun chat(
        messages: List<ConversationMessage>,
        contextChunks: List<String>,
    ): String {
        val resolvedModel = aiProperties.modelName
        val request = buildRequest(messages, contextChunks)

        try {
            return retryWithBackoff(
                maxAttempts = aiProperties.maxRetries,
                retryOn = { it !is BusinessException },
            ) {
                val startTime = System.currentTimeMillis()
                log.info("Calling Gemini API: model={}", resolvedModel)

                val response = aiWebClient.post()
                    .uri("/v1beta/models/{model}:generateContent", resolvedModel)
                    .header("x-goog-api-key", aiProperties.apiKey)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus({ it.is4xxClientError }) { _ ->
                        log.warn("Gemini API 4xx error (non-retryable): model={}", resolvedModel)
                        Mono.error(BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE))
                    }
                    .awaitBody<GeminiChatResponse>()

                val elapsed = System.currentTimeMillis() - startTime
                log.info("Gemini API success: model={}, elapsedMs={}", resolvedModel, elapsed)

                response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: throw BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE)
            }
        } catch (e: BusinessException) {
            throw e
        } catch (e: Exception) {
            log.error("Gemini API error after all retries: model={}, error={}", resolvedModel, e.message)
            throw BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE)
        }
    }

    override fun chatStream(
        messages: List<ConversationMessage>,
        contextChunks: List<String>,
    ): Flow<String> {
        val resolvedModel = aiProperties.modelName
        val request = buildRequest(messages, contextChunks)

        return flow {
            log.info("Starting Gemini stream: model={}", resolvedModel)
            aiWebClient.post()
                .uri("/v1beta/models/{model}:streamGenerateContent?alt=sse", resolvedModel)
                .header("x-goog-api-key", aiProperties.apiKey)
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(String::class.java)
                .toIterable()
                .forEach { chunk ->
                    if (chunk.startsWith("data:")) {
                        val data = chunk.removePrefix("data:").trim()
                        if (data.isNotEmpty() && data != "[DONE]") {
                            try {
                                val response = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
                                    .readValue(data, GeminiChatResponse::class.java)
                                val text = response.candidates
                                    .firstOrNull()?.content?.parts?.firstOrNull()?.text
                                if (!text.isNullOrEmpty()) emit(text)
                            } catch (_: Exception) {
                                // skip unparseable SSE events
                            }
                        }
                    }
                }
        }
    }

    private fun buildRequest(
        messages: List<ConversationMessage>,
        contextChunks: List<String>,
    ): GeminiChatRequest {
        val systemPrompt = systemPromptBuilder.build(contextChunks)
        return GeminiChatRequest(
            contents = messages.map { msg ->
                GeminiContent(
                    role = msg.role,
                    parts = listOf(GeminiPart(text = msg.content)),
                )
            },
            systemInstruction = GeminiSystemInstruction(
                parts = listOf(GeminiPart(text = systemPrompt)),
            ),
        )
    }
}
