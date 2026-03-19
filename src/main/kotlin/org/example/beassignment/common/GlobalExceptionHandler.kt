package org.example.beassignment.common

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ApiResponse<Nothing>> {
        log.warn("Business exception: code={}, message={}", e.errorCode, e.message)
        return ResponseEntity
            .status(e.errorCode.httpStatus)
            .body(ApiResponse.error(e.errorCode))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val details = e.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        log.warn("Validation failed: {}", details)
        return ResponseEntity
            .status(ErrorCode.INVALID_REQUEST.httpStatus)
            .body(ApiResponse.error(ErrorCode.INVALID_REQUEST))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleMessageNotReadable(e: HttpMessageNotReadableException): ResponseEntity<ApiResponse<Nothing>> {
        log.warn("Malformed request body: {}", e.message)
        return ResponseEntity
            .status(ErrorCode.INVALID_REQUEST.httpStatus)
            .body(ApiResponse.error(ErrorCode.INVALID_REQUEST))
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(e: AccessDeniedException): ResponseEntity<ApiResponse<Nothing>> {
        log.warn("Access denied: {}", e.message)
        return ResponseEntity
            .status(ErrorCode.FORBIDDEN.httpStatus)
            .body(ApiResponse.error(ErrorCode.FORBIDDEN))
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(e: Exception): ResponseEntity<ApiResponse<Nothing>> {
        log.error("Unexpected error", e)
        return ResponseEntity
            .status(ErrorCode.INTERNAL_SERVER_ERROR.httpStatus)
            .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR))
    }
}
