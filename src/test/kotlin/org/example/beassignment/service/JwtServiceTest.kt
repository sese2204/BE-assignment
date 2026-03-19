package org.example.beassignment.service

import org.example.beassignment.config.JwtProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class JwtServiceTest {

    private val properties = JwtProperties(
        secret = "test-secret-key-that-is-at-least-256-bits-long-for-hmac-sha",
        expiryMs = 3600000,
    )
    private val jwtService = JwtService(properties)

    @Test
    fun `generateToken and validateToken round-trip`() {
        val token = jwtService.generateToken(userId = 1L, role = "member")
        val claims = jwtService.validateToken(token)

        assertNotNull(claims)
        assertEquals(1L, claims!!.userId)
        assertEquals("member", claims.role)
    }

    @Test
    fun `validateToken returns null for invalid token`() {
        val result = jwtService.validateToken("invalid.token.here")
        assertNull(result)
    }

    @Test
    fun `validateToken returns null for expired token`() {
        val expiredProperties = JwtProperties(
            secret = "test-secret-key-that-is-at-least-256-bits-long-for-hmac-sha",
            expiryMs = -1000,
        )
        val expiredJwtService = JwtService(expiredProperties)

        val token = expiredJwtService.generateToken(userId = 1L, role = "member")
        val result = jwtService.validateToken(token)

        assertNull(result)
    }

    @Test
    fun `validateToken returns null for tampered token`() {
        val token = jwtService.generateToken(userId = 1L, role = "member")
        val tampered = token.dropLast(5) + "xxxxx"

        val result = jwtService.validateToken(tampered)
        assertNull(result)
    }

    @Test
    fun `token contains correct role for admin`() {
        val token = jwtService.generateToken(userId = 99L, role = "admin")
        val claims = jwtService.validateToken(token)

        assertNotNull(claims)
        assertEquals(99L, claims!!.userId)
        assertEquals("admin", claims.role)
    }
}
