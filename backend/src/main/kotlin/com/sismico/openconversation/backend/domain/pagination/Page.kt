package com.sismico.openconversation.backend.domain.pagination

data class Page<T>(
    val data: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)
