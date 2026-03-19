package org.example.beassignment.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.example.beassignment.common.ApiResponse
import org.example.beassignment.dto.LoginRequest
import org.example.beassignment.dto.LoginResponse
import org.example.beassignment.dto.SignupRequest
import org.example.beassignment.dto.SignupResponse
import org.example.beassignment.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Auth", description = "회원가입 및 로그인")
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(private val authService: AuthService) {

    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 이름으로 새 계정을 생성합니다. role 파라미터로 admin 계정 생성이 가능합니다. ⚠️ 프로덕션 배포 시 admin 역할 선택 기능은 제거 필요")
    @PostMapping("/signup")
    fun signup(@Valid @RequestBody request: SignupRequest): ResponseEntity<ApiResponse<SignupResponse>> {
        val result = authService.signup(request)
        return ResponseEntity.ok(ApiResponse.ok(result))
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 JWT 토큰을 발급받습니다.")
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<ApiResponse<LoginResponse>> {
        val result = authService.login(request)
        return ResponseEntity.ok(ApiResponse.ok(result))
    }
}
