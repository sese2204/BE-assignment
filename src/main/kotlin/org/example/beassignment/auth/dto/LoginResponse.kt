package org.example.beassignment.auth.dto

data class LoginResponse(
    val token: String,
    val expiresIn: Long,
)
