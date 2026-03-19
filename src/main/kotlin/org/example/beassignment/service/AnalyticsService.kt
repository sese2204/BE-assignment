package org.example.beassignment.service

import org.example.beassignment.dto.ActivitySummaryResponse
import org.example.beassignment.entity.EventType
import org.example.beassignment.repository.ActivityLogRepository
import org.example.beassignment.repository.ThreadRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class AnalyticsService(
    private val activityLogRepository: ActivityLogRepository,
    private val threadRepository: ThreadRepository,
) {

    @Transactional(readOnly = true)
    fun getActivitySummary(): ActivitySummaryResponse {
        val now = Instant.now()
        val cutoff = now.minus(24, ChronoUnit.HOURS)

        return ActivitySummaryResponse(
            signupCount = activityLogRepository.countByEventTypeAndCreatedAtAfter(EventType.SIGNUP, cutoff),
            loginCount = activityLogRepository.countByEventTypeAndCreatedAtAfter(EventType.LOGIN, cutoff),
            threadCreateCount = activityLogRepository.countByEventTypeAndCreatedAtAfter(EventType.THREAD_CREATE, cutoff),
            periodStart = cutoff,
            periodEnd = now,
        )
    }

    @Transactional(readOnly = true)
    fun generateCsvReport(): ByteArray {
        val cutoff = Instant.now().minus(24, ChronoUnit.HOURS)
        val threads = threadRepository.findByCreatedAtAfterWithUser(cutoff)

        val sb = StringBuilder()
        sb.appendLine("thread_id,user_email,user_name,created_at")
        for (thread in threads) {
            sb.appendLine("${thread.id},${thread.user.email},${thread.user.name},${thread.createdAt}")
        }
        return sb.toString().toByteArray(Charsets.UTF_8)
    }
}
