package com.sismico.openconversation.backend.adapters.`in`.web

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api")
class AnalyzeController {
    @PostMapping("/analyse", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun analyse(): ResponseEntity<Void> =
        ResponseEntity
            .status(HttpStatus.PERMANENT_REDIRECT)
            .location(URI.create("/api/v1/conversations"))
            .build()
}
