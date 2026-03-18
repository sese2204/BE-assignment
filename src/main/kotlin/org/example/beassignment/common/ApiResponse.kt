package org.example.beassignment.common

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?,
) {
    companion object {
        fun <T> ok(data: T): ApiResponse<T> =
            ApiResponse(success = true, data = data, message = null)

        fun error(code: ErrorCode): ApiResponse<Nothing> =
            ApiResponse(success = false, data = null, message = code.name)
    }
}
