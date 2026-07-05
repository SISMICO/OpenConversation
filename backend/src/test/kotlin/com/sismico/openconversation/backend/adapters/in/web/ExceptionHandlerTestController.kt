package com.sismico.openconversation.backend.adapters.`in`.web

import com.sismico.openconversation.backend.adapters.`in`.web.error.NotFoundException
import com.sismico.openconversation.backend.adapters.`in`.web.error.ValidationException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/test")
class ExceptionHandlerTestController {
    @PostMapping("/validation-error")
    fun validationError(): String = throw ValidationException("Invalid request")

    @GetMapping("/not-found")
    fun notFound(): String = throw NotFoundException("Resource not found")

    @GetMapping("/illegal-argument")
    fun illegalArgument(): String = throw IllegalArgumentException("Bad input")
}
