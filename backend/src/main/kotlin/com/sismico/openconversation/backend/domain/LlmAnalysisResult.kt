package com.sismico.openconversation.backend.domain

data class LlmAnalysisResult(
    val feedbackItems: List<LlmFeedbackItem> = emptyList(),
    val overallComment: String? = null,
)

data class LlmFeedbackItem(
    val excerpt: String,
    val correctedExcerpt: String,
    val explanation: String,
)
