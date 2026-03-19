package org.example.beassignment.chat.builder

import org.example.beassignment.config.AiProperties
import org.springframework.stereotype.Component

@Component
class SystemPromptBuilder(private val aiProperties: AiProperties) {

    fun build(contextChunks: List<String> = emptyList()): String {
        if (contextChunks.isEmpty()) return aiProperties.systemPrompt
        val context = contextChunks.joinToString("\n\n")
        return """
            |${aiProperties.systemPrompt}
            |
            |Relevant context:
            |$context
        """.trimMargin()
    }
}
