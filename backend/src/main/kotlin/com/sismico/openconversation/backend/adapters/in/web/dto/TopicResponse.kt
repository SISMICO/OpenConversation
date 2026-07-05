package com.sismico.openconversation.backend.adapters.`in`.web.dto

import java.time.OffsetDateTime
import java.util.UUID

data class TopicResponse(
    val id: UUID,
    val title: String,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
)
