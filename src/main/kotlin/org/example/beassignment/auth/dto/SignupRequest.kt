package org.example.beassignment.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class SignupRequest(
    @field:Email(message = "올바른 이메일 형식이어야 합니다")
    @field:NotBlank(message = "이메일은 필수입니다")
    val email: String,

    @field:Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
    @field:NotBlank(message = "비밀번호는 필수입니다")
    val password: String,

    @field:NotBlank(message = "이름은 필수입니다")
    val name: String,

    @Schema(description = "역할 (member 또는 admin). admin은 프로덕션 배포 시 제거 필요", example = "member")
    @field:Pattern(regexp = "member|admin", message = "역할은 member 또는 admin만 허용됩니다")
    val role: String = "member",
)
