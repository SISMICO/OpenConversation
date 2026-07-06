package com.sismico.openconversation.backend.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("openconversation.llm")
data class LlmProperties(
    val baseUrl: String,
    val apiKey: String = "",
    val model: String,
    val timeoutSeconds: Long = DEFAULT_TIMEOUT_SECONDS,
) {
    companion object {
        private const val DEFAULT_TIMEOUT_SECONDS = 120L
    }
}
