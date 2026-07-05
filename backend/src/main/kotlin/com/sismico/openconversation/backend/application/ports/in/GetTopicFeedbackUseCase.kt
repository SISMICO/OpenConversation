package com.sismico.openconversation.backend.application.ports.`in`

import com.sismico.openconversation.backend.domain.ConversationFeedbackGroup
import com.sismico.openconversation.backend.domain.pagination.Page
import java.util.UUID

interface GetTopicFeedbackUseCase {
    fun getFeedbackByTopicId(
        topicId: UUID,
        page: Int,
        size: Int,
    ): Page<ConversationFeedbackGroup>
}
