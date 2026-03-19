package org.example.beassignment.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

enum class RoleType {
    member,
    admin,
}

data class SignupRequest(
    @field:Email(message = "올바른 이메일 형식이어야 합니다")
    @field:NotBlank(message = "이메일은 필수입니다")
    val email: String,

    @field:Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
    @field:NotBlank(message = "비밀번호는 필수입니다")
    val password: String,

    @field:NotBlank(message = "이름은 필수입니다")
    val name: String,

    @Schema(description = "역할 선택 (member: 일반 사용자, admin: 관리자)", example = "member")
    @field:NotNull
    val role: RoleType = RoleType.member,
)
