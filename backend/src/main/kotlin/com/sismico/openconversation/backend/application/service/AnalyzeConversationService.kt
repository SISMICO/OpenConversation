package com.sismico.openconversation.backend.application.service

import com.sismico.openconversation.backend.application.ports.`in`.AnalyzeConversationUseCase
import com.sismico.openconversation.backend.application.ports.out.llm.LlmAnalysisPort
import com.sismico.openconversation.backend.application.ports.out.persistence.ConversationRepositoryPort
import com.sismico.openconversation.backend.application.ports.out.storage.AudioStoragePort
import com.sismico.openconversation.backend.application.ports.out.transcription.AudioConversionPort
import com.sismico.openconversation.backend.application.ports.out.transcription.TranscriptionPort
import com.sismico.openconversation.backend.domain.Conversation
import com.sismico.openconversation.backend.domain.ConversationWithTopic
import com.sismico.openconversation.backend.domain.FeedbackItem
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Service
@Transactional
class AnalyzeConversationService(
    private val topicService: TopicService,
    private val conversationRepository: ConversationRepositoryPort,
    private val audioStoragePort: AudioStoragePort,
    private val audioConversionPort: AudioConversionPort,
    private val transcriptionPort: TranscriptionPort,
    private val llmAnalysisPort: LlmAnalysisPort,
) : AnalyzeConversationUseCase {
    override fun analyze(
        audio: ByteArray,
        audioFilename: String?,
        topicTitle: String,
        language: String?,
    ): ConversationWithTopic {
        val topic = topicService.ensureTopic(topicTitle)
        val storageRef = audioStoragePort.store(audio, audioFilename)
        val audioForTranscription = audioConversionPort.convert(audio, audioFilename)
        val transcription = transcriptionPort.transcribe(audioForTranscription, language)
        val now = OffsetDateTime.now(ZoneOffset.UTC)

        val llmResult = llmAnalysisPort.analyze(transcription.text, topic.title, language)

        val feedbackItems =
            llmResult.feedbackItems.mapIndexed { index, item ->
                FeedbackItem(
                    excerpt = item.excerpt,
                    recommendation = "Correction: ${item.correctedExcerpt}\n${item.explanation}",
                    displayOrder = index,
                    createdAt = now,
                )
            }

        val conversation =
            Conversation(
                topicId = requireNotNull(topic.id) { "Topic id must not be null" },
                audioStorageRef = storageRef.ref,
                transcript = transcription.text,
                analyzedAt = now,
                feedbackItems = feedbackItems,
                createdAt = now,
                updatedAt = now,
            )

        val saved = conversationRepository.save(conversation)
        return ConversationWithTopic(saved, topic.title)
    }
}
