package com.sismico.openconversation.backend.domain

import java.time.OffsetDateTime
import java.util.UUID

data class FeedbackItem(
    val id: UUID? = null,
    val excerpt: String,
    val recommendation: String,
    val displayOrder: Int,
    val createdAt: OffsetDateTime? = null,
)
