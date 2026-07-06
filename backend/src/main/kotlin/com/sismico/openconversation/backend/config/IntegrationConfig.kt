package com.sismico.openconversation.backend.config

import com.sismico.openconversation.backend.adapters.out.llm.OpenAiCompatibleLlmAdapter
import com.sismico.openconversation.backend.adapters.out.storage.LocalAudioStorageAdapter
import com.sismico.openconversation.backend.adapters.out.transcription.FfmpegAudioConversionAdapter
import com.sismico.openconversation.backend.adapters.out.transcription.WhisperTranscriptionAdapter
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.web.client.RestClient
import tools.jackson.databind.ObjectMapper
import java.time.Duration

@Configuration
@EnableAsync
@EnableConfigurationProperties(
    WhisperProperties::class,
    LocalAudioStorageProperties::class,
    LlmProperties::class,
)
class IntegrationConfig {
    @Bean
    fun restClient(): RestClient = RestClient.builder().build()

    @Bean
    fun whisperTranscriptionAdapter(
        restClient: RestClient,
        whisperProperties: WhisperProperties,
    ): WhisperTranscriptionAdapter = WhisperTranscriptionAdapter(restClient, whisperProperties)

    @Bean
    fun openAiCompatibleLlmAdapter(
        llmProperties: LlmProperties,
        objectMapper: ObjectMapper,
    ): OpenAiCompatibleLlmAdapter {
        val requestFactory =
            JdkClientHttpRequestFactory().apply {
                setReadTimeout(Duration.ofSeconds(llmProperties.timeoutSeconds))
            }
        val llmRestClient = RestClient.builder().requestFactory(requestFactory).build()
        return OpenAiCompatibleLlmAdapter(llmRestClient, llmProperties, objectMapper)
    }

    @Bean
    fun localAudioStorageAdapter(localAudioStorageProperties: LocalAudioStorageProperties): LocalAudioStorageAdapter =
        LocalAudioStorageAdapter(localAudioStorageProperties)

    @Bean
    fun ffmpegAudioConversionAdapter(): FfmpegAudioConversionAdapter = FfmpegAudioConversionAdapter()
}
