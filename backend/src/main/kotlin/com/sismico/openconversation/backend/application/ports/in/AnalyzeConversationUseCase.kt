package com.sismico.openconversation.backend.application.ports.`in`

import com.sismico.openconversation.backend.domain.ConversationWithTopic

interface AnalyzeConversationUseCase {
    fun analyze(
        audio: ByteArray,
        audioFilename: String?,
        topicTitle: String,
        language: String?,
    ): ConversationWithTopic
}
