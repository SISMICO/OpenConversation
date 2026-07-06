package com.sismico.openconversation.backend.domain.exception

class LlmAnalysisException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
