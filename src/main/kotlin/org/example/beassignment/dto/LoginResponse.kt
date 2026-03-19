package org.example.beassignment.dto

data class LoginResponse(
    val token: String,
    val expiresIn: Long,
)
