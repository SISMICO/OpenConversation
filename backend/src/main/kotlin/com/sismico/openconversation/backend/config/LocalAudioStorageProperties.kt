package com.sismico.openconversation.backend.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("openconversation.audio-storage.local")
data class LocalAudioStorageProperties(
    val basePath: String,
)
