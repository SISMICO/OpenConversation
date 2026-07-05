package com.sismico.openconversation.backend.adapters.`in`.web.dto

data class PageResponse<T>(
    val data: List<T>,
    val pagination: PaginationMetaResponse,
)

data class PaginationMetaResponse(
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)
