package com.sismico.openconversation.backend.application.ports.`in`

import com.sismico.openconversation.backend.domain.Topic
import com.sismico.openconversation.backend.domain.pagination.Page

interface SearchTopicsUseCase {
    fun search(
        query: String?,
        page: Int,
        size: Int,
    ): Page<Topic>
}
