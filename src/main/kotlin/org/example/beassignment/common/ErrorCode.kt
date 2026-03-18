package org.example.beassignment.common

import org.springframework.http.HttpStatus

enum class ErrorCode(val httpStatus: HttpStatus) {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND),
    AI_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
}
