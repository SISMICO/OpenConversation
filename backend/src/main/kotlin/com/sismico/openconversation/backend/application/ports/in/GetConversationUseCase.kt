package com.sismico.openconversation.backend.application.ports.`in`

import com.sismico.openconversation.backend.domain.ConversationWithTopic
import java.util.UUID

interface GetConversationUseCase {
    fun getById(id: UUID): ConversationWithTopic?
}
