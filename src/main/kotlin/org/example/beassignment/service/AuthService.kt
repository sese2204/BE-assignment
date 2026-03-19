package org.example.beassignment.service

import org.example.beassignment.common.BusinessException
import org.example.beassignment.common.ErrorCode
import org.example.beassignment.config.JwtProperties
import org.example.beassignment.dto.LoginRequest
import org.example.beassignment.dto.LoginResponse
import org.example.beassignment.dto.SignupRequest
import org.example.beassignment.dto.SignupResponse
import org.example.beassignment.entity.ActivityLog
import org.example.beassignment.entity.EventType
import org.example.beassignment.entity.User
import org.example.beassignment.repository.ActivityLogRepository
import org.example.beassignment.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val jwtProperties: JwtProperties,
    private val activityLogRepository: ActivityLogRepository,
) {
    @Transactional
    fun signup(request: SignupRequest): SignupResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw BusinessException(ErrorCode.DUPLICATE_EMAIL)
        }
        val user = userRepository.save(
            User(
                email = request.email,
                passwordHash = passwordEncoder.encode(request.password),
                name = request.name,
                role = request.role.name,
            ),
        )
        activityLogRepository.save(ActivityLog(user = user, eventType = EventType.SIGNUP))
        return SignupResponse(userId = user.id)
    }

    @Transactional
    fun login(request: LoginRequest): LoginResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw BusinessException(ErrorCode.INVALID_CREDENTIALS)
        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw BusinessException(ErrorCode.INVALID_CREDENTIALS)
        }
        activityLogRepository.save(ActivityLog(user = user, eventType = EventType.LOGIN))
        val token = jwtService.generateToken(user.id, user.role)
        return LoginResponse(token = token, expiresIn = jwtProperties.expiryMs / 1000)
    }
}
