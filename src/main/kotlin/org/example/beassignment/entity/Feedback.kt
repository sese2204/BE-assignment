package org.example.beassignment.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

enum class FeedbackStatus {
    PENDING,
    RESOLVED,
}

@Entity
@Table(
    name = "feedbacks",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "thread_id"])],
)
class Feedback(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", nullable = false)
    val thread: Thread,

    @Column(nullable = false)
    val isPositive: Boolean,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: FeedbackStatus = FeedbackStatus.PENDING,

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),
)
