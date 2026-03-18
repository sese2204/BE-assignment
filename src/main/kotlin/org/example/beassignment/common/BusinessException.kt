package org.example.beassignment.common

class BusinessException(
    val errorCode: ErrorCode,
    message: String = errorCode.name,
) : RuntimeException(message)
