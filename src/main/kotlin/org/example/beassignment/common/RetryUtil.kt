package org.example.beassignment.common

import kotlinx.coroutines.delay
import kotlin.math.pow

suspend fun <T> retryWithBackoff(
    maxAttempts: Int = 3,
    initialDelayMs: Long = 1000L,
    multiplier: Double = 2.0,
    retryOn: (Throwable) -> Boolean = { true },
    block: suspend () -> T,
): T {
    var lastException: Throwable? = null
    repeat(maxAttempts) { attempt ->
        try {
            return block()
        } catch (e: Throwable) {
            if (!retryOn(e)) throw e
            lastException = e
            if (attempt < maxAttempts - 1) {
                delay((initialDelayMs * multiplier.pow(attempt)).toLong())
            }
        }
    }
    throw lastException!!
}
