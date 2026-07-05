package com.sismico.openconversation.backend.adapters.`in`.web.dto

import java.util.UUID

data class FeedbackItemResponse(
    val id: UUID,
    val excerpt: String,
    val recommendation: String,
    val displayOrder: Int,
)
