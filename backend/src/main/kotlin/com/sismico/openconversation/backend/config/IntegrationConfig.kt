package com.sismico.openconversation.backend.config

import com.sismico.openconversation.backend.adapters.out.storage.LocalAudioStorageAdapter
import com.sismico.openconversation.backend.adapters.out.transcription.FfmpegAudioConversionAdapter
import com.sismico.openconversation.backend.adapters.out.transcription.WhisperTranscriptionAdapter
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(WhisperProperties::class, LocalAudioStorageProperties::class)
class IntegrationConfig {
    @Bean
    fun restClient(): RestClient = RestClient.builder().build()

    @Bean
    fun whisperTranscriptionAdapter(
        restClient: RestClient,
        whisperProperties: WhisperProperties,
    ): WhisperTranscriptionAdapter = WhisperTranscriptionAdapter(restClient, whisperProperties)

    @Bean
    fun localAudioStorageAdapter(localAudioStorageProperties: LocalAudioStorageProperties): LocalAudioStorageAdapter =
        LocalAudioStorageAdapter(localAudioStorageProperties)

    @Bean
    fun ffmpegAudioConversionAdapter(): FfmpegAudioConversionAdapter = FfmpegAudioConversionAdapter()
}
