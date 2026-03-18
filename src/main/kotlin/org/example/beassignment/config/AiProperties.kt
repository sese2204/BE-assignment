package org.example.beassignment.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

@Component
@Validated
@ConfigurationProperties(prefix = "ai")
data class AiProperties(
    val apiKey: String,
    val baseUrl: String,
    val modelName: String,
    val systemPrompt: String,
    val connectTimeoutMs: Long,
    val readTimeoutMs: Long,
    val maxRetries: Int,
)
