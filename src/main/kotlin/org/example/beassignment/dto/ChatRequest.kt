package org.example.beassignment.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ChatRequest(
    @field:NotBlank(message = "message must not be blank")
    @field:Size(max = 4000, message = "message must not exceed 4000 characters")
    val message: String,
)
