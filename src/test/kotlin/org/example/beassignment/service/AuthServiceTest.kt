package org.example.beassignment.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.example.beassignment.common.BusinessException
import org.example.beassignment.common.ErrorCode
import org.example.beassignment.config.JwtProperties
import org.example.beassignment.dto.LoginRequest
import org.example.beassignment.dto.SignupRequest
import org.example.beassignment.entity.User
import org.example.beassignment.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.Optional

class AuthServiceTest {

    private val userRepository = mockk<UserRepository>()
    private val passwordEncoder = mockk<PasswordEncoder>()
    private val jwtService = mockk<JwtService>()
    private val jwtProperties = JwtProperties(secret = "unused", expiryMs = 3600000)

    private val authService = AuthService(userRepository, passwordEncoder, jwtService, jwtProperties)

    @Test
    fun `signup succeeds with valid request`() {
        val request = SignupRequest(email = "test@test.com", password = "password123", name = "Test")

        every { userRepository.existsByEmail("test@test.com") } returns false
        every { passwordEncoder.encode("password123") } returns "hashed"
        every { userRepository.save(any()) } returns User(
            id = 1L, email = "test@test.com", passwordHash = "hashed", name = "Test",
        )

        val result = authService.signup(request)

        assertEquals(1L, result.userId)
        verify { userRepository.save(any()) }
    }

    @Test
    fun `signup with admin role`() {
        val request = SignupRequest(email = "admin@test.com", password = "password123", name = "Admin", role = "admin")

        every { userRepository.existsByEmail("admin@test.com") } returns false
        every { passwordEncoder.encode("password123") } returns "hashed"
        every { userRepository.save(match { it.role == "admin" }) } returns User(
            id = 2L, email = "admin@test.com", passwordHash = "hashed", name = "Admin", role = "admin",
        )

        val result = authService.signup(request)

        assertEquals(2L, result.userId)
        verify { userRepository.save(match { it.role == "admin" }) }
    }

    @Test
    fun `signup throws DUPLICATE_EMAIL for existing email`() {
        val request = SignupRequest(email = "dup@test.com", password = "password123", name = "Dup")

        every { userRepository.existsByEmail("dup@test.com") } returns true

        val ex = assertThrows<BusinessException> { authService.signup(request) }
        assertEquals(ErrorCode.DUPLICATE_EMAIL, ex.errorCode)
    }

    @Test
    fun `login succeeds with correct credentials`() {
        val request = LoginRequest(email = "test@test.com", password = "password123")
        val user = User(id = 1L, email = "test@test.com", passwordHash = "hashed", name = "Test")

        every { userRepository.findByEmail("test@test.com") } returns user
        every { passwordEncoder.matches("password123", "hashed") } returns true
        every { jwtService.generateToken(1L, "member") } returns "jwt-token"

        val result = authService.login(request)

        assertEquals("jwt-token", result.token)
        assertEquals(3600L, result.expiresIn)
    }

    @Test
    fun `login throws INVALID_CREDENTIALS for non-existent user`() {
        val request = LoginRequest(email = "no@test.com", password = "password123")

        every { userRepository.findByEmail("no@test.com") } returns null

        val ex = assertThrows<BusinessException> { authService.login(request) }
        assertEquals(ErrorCode.INVALID_CREDENTIALS, ex.errorCode)
    }

    @Test
    fun `login throws INVALID_CREDENTIALS for wrong password`() {
        val request = LoginRequest(email = "test@test.com", password = "wrong")
        val user = User(id = 1L, email = "test@test.com", passwordHash = "hashed", name = "Test")

        every { userRepository.findByEmail("test@test.com") } returns user
        every { passwordEncoder.matches("wrong", "hashed") } returns false

        val ex = assertThrows<BusinessException> { authService.login(request) }
        assertEquals(ErrorCode.INVALID_CREDENTIALS, ex.errorCode)
    }
}
