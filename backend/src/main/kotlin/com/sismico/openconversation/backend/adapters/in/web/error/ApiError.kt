package com.sismico.openconversation.backend.adapters.`in`.web.error

data class ApiErrorResponse(
    val error: ApiError,
)

data class ApiError(
    val code: String,
    val message: String,
    val details: List<String> = emptyList(),
)
