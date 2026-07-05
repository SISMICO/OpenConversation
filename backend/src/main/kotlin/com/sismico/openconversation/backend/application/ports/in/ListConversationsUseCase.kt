package com.sismico.openconversation.backend.application.ports.`in`

import com.sismico.openconversation.backend.application.ports.out.persistence.ConversationFilters
import com.sismico.openconversation.backend.domain.ConversationSummary
import com.sismico.openconversation.backend.domain.pagination.Page

interface ListConversationsUseCase {
    fun list(
        filters: ConversationFilters,
        page: Int,
        size: Int,
    ): Page<ConversationSummary>
}
