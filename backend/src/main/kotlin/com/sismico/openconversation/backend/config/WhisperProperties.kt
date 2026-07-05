package com.sismico.openconversation.backend.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("openconversation.whisper")
data class WhisperProperties(
    val baseUrl: String,
)
