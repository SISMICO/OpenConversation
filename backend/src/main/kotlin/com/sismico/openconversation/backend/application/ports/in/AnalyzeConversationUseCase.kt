package com.sismico.openconversation.backend.application.ports.`in`

import com.sismico.openconversation.backend.domain.ConversationWithTopic

interface AnalyzeConversationUseCase {
    fun analyze(
        audioStorageRef: String,
        topicTitle: String,
    ): ConversationWithTopic
}
