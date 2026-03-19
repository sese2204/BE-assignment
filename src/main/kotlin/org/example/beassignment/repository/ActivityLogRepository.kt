package org.example.beassignment.repository

import org.example.beassignment.entity.ActivityLog
import org.example.beassignment.entity.EventType
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

interface ActivityLogRepository : JpaRepository<ActivityLog, Long> {

    fun countByEventTypeAndCreatedAtAfter(eventType: EventType, createdAt: Instant): Long
}
