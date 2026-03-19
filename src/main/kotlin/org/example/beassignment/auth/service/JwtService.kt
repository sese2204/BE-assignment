package org.example.beassignment.auth.service

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.example.beassignment.config.JwtProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.util.Date

data class JwtClaims(val userId: Long, val role: String)

@Service
class JwtService(private val jwtProperties: JwtProperties) {

    private val log = LoggerFactory.getLogger(javaClass)

    private val signingKey by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray(StandardCharsets.UTF_8))
    }

    fun generateToken(userId: Long, role: String): String {
        val now = Date()
        val expiry = Date(now.time + jwtProperties.expiryMs)
        return Jwts.builder()
            .subject(userId.toString())
            .claim("role", role)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(signingKey)
            .compact()
    }

    fun validateToken(token: String): JwtClaims? {
        return try {
            val claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .payload
            JwtClaims(
                userId = claims.subject.toLong(),
                role = claims["role"] as String,
            )
        } catch (e: JwtException) {
            log.debug("Invalid JWT token: {}", e.message)
            null
        } catch (e: IllegalArgumentException) {
            log.debug("JWT claims are empty: {}", e.message)
            null
        }
    }
}
