package com.sismico.openconversation.backend.application.ports.out.llm

import com.sismico.openconversation.backend.domain.LlmAnalysisResult

interface LlmAnalysisPort {
    fun analyze(
        transcript: String,
        topic: String,
        language: String?,
    ): LlmAnalysisResult
}
