package com.sismico.openconversation.backend.adapters.`in`.web.error

class NotFoundException(
    message: String,
) : RuntimeException(message)

class ValidationException(
    message: String,
) : RuntimeException(message)

class PayloadTooLargeException(
    message: String,
) : RuntimeException(message)
