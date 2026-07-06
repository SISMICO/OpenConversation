package com.sismico.openconversation.backend.application.service

import com.sismico.openconversation.backend.application.ports.out.llm.LlmAnalysisPort
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class LlmWarmupService(
    private val llmAnalysisPort: LlmAnalysisPort,
) {
    private val logger = LoggerFactory.getLogger(LlmWarmupService::class.java)

    @Async
    @EventListener(ApplicationReadyEvent::class)
    fun warmup(event: ApplicationReadyEvent) {
        logger.info("Starting LLM warm-up against configured provider")

        try {
            llmAnalysisPort.analyze(
                transcript = "This is a warm-up prompt to preload the language model.",
                topic = "warm-up",
                language = null,
            )
            logger.info("LLM warm-up completed successfully")
        } catch (ex: Exception) {
            logger.error("LLM warm-up failed: ${ex.message}", ex)
        }
    }
}
