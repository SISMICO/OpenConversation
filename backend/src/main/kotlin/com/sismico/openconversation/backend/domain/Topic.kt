package com.sismico.openconversation.backend.domain

import java.time.OffsetDateTime
import java.util.UUID

data class Topic(
    val id: UUID? = null,
    val title: String,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null,
)
