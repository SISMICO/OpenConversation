package com.sismico.openconversation.backend.application.service

import com.sismico.openconversation.backend.application.ports.out.persistence.ConversationRepositoryPort
import com.sismico.openconversation.backend.application.ports.out.storage.AudioStoragePort
import com.sismico.openconversation.backend.application.ports.out.transcription.AudioConversionPort
import com.sismico.openconversation.backend.application.ports.out.transcription.TranscriptionPort
import com.sismico.openconversation.backend.domain.AudioStorageReference
import com.sismico.openconversation.backend.domain.Conversation
import com.sismico.openconversation.backend.domain.ConversationWithTopic
import com.sismico.openconversation.backend.domain.Topic
import com.sismico.openconversation.backend.domain.Transcription
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

class AnalyzeConversationServiceTest {
    private val topicService: TopicService = mockk()
    private val conversationRepository: ConversationRepositoryPort = mockk()
    private val audioStoragePort: AudioStoragePort = mockk()
    private val audioConversionPort: AudioConversionPort = mockk()
    private val transcriptionPort: TranscriptionPort = mockk()
    private val analyzeConversationService =
        AnalyzeConversationService(
            topicService,
            conversationRepository,
            audioStoragePort,
            audioConversionPort,
            transcriptionPort,
        )

    @Test
    fun `analyze stores audio, transcribes it, and persists conversation`() {
        val topicId = UUID.randomUUID()
        val topic =
            Topic(
                id = topicId,
                title = "job interview about my career",
                createdAt = OffsetDateTime.now(ZoneOffset.UTC),
                updatedAt = OffsetDateTime.now(ZoneOffset.UTC),
            )
        val audio = byteArrayOf(1, 2, 3)
        val convertedAudio = byteArrayOf(4, 5, 6)
        val audioFilename = "recording.webm"
        val storageRef = AudioStorageReference("local:///tmp/audio/abc.webm")
        val transcription = Transcription("Simulated transcription for topic 'job interview about my career'.")
        val savedConversation =
            Conversation(
                id = UUID.randomUUID(),
                topicId = topicId,
                audioStorageRef = storageRef.ref,
                transcript = transcription.text,
                feedbackItems = emptyList(),
                createdAt = OffsetDateTime.now(ZoneOffset.UTC),
                updatedAt = OffsetDateTime.now(ZoneOffset.UTC),
            )

        every { topicService.ensureTopic("job interview about my career") } returns topic
        every { audioStoragePort.store(audio, audioFilename) } returns storageRef
        every { audioConversionPort.convert(audio, audioFilename) } returns convertedAudio
        every { transcriptionPort.transcribe(convertedAudio, null) } returns transcription
        every { conversationRepository.save(any()) } returns savedConversation

        val result =
            analyzeConversationService.analyze(
                audio = audio,
                audioFilename = audioFilename,
                topicTitle = "job interview about my career",
                language = null,
            )

        assertEquals(ConversationWithTopic(savedConversation, "job interview about my career"), result)
        verify { audioStoragePort.store(audio, audioFilename) }
        verify { audioConversionPort.convert(audio, audioFilename) }
        verify { transcriptionPort.transcribe(convertedAudio, null) }
        val conversationSlot = slot<Conversation>()
        verify { conversationRepository.save(capture(conversationSlot)) }
        assertEquals(topicId, conversationSlot.captured.topicId)
        assertEquals(storageRef.ref, conversationSlot.captured.audioStorageRef)
        assertEquals(transcription.text, conversationSlot.captured.transcript)
        assertNotNull(conversationSlot.captured.analyzedAt)
    }

    @Test
    fun `analyze passes topic title to topic service`() {
        val topicId = UUID.randomUUID()
        val topic =
            Topic(
                id = topicId,
                title = "job interview",
                createdAt = OffsetDateTime.now(ZoneOffset.UTC),
                updatedAt = OffsetDateTime.now(ZoneOffset.UTC),
            )
        val audio = byteArrayOf(4, 5, 6)
        val convertedAudio = byteArrayOf(7, 8, 9)
        val storageRef = AudioStorageReference("local:///tmp/audio/ref.webm")
        val transcription = Transcription("Transcript")
        val savedConversation =
            Conversation(
                id = UUID.randomUUID(),
                topicId = topicId,
                audioStorageRef = storageRef.ref,
                transcript = transcription.text,
                feedbackItems = emptyList(),
                createdAt = OffsetDateTime.now(ZoneOffset.UTC),
                updatedAt = OffsetDateTime.now(ZoneOffset.UTC),
            )

        every { topicService.ensureTopic("job interview") } returns topic
        every { audioStoragePort.store(audio, null) } returns storageRef
        every { audioConversionPort.convert(audio, null) } returns convertedAudio
        every { transcriptionPort.transcribe(convertedAudio, null) } returns transcription
        every { conversationRepository.save(any()) } returns savedConversation

        val result =
            analyzeConversationService.analyze(
                audio = audio,
                audioFilename = null,
                topicTitle = "job interview",
                language = null,
            )

        assertEquals(topic.title, result.topicTitle)
        verify { topicService.ensureTopic("job interview") }
    }

    @Test
    fun `analyze includes language in feedback recommendations when provided`() {
        val topicId = UUID.randomUUID()
        val topic =
            Topic(
                id = topicId,
                title = "job interview",
                createdAt = OffsetDateTime.now(ZoneOffset.UTC),
                updatedAt = OffsetDateTime.now(ZoneOffset.UTC),
            )
        val audio = byteArrayOf(7, 8, 9)
        val convertedAudio = byteArrayOf(10, 11, 12)
        val storageRef = AudioStorageReference("local:///tmp/audio/ref.webm")
        val transcription = Transcription("Transcript")
        val savedConversation =
            Conversation(
                id = UUID.randomUUID(),
                topicId = topicId,
                audioStorageRef = storageRef.ref,
                transcript = transcription.text,
                feedbackItems = emptyList(),
                createdAt = OffsetDateTime.now(ZoneOffset.UTC),
                updatedAt = OffsetDateTime.now(ZoneOffset.UTC),
            )

        every { topicService.ensureTopic("job interview") } returns topic
        every { audioStoragePort.store(audio, null) } returns storageRef
        every { audioConversionPort.convert(audio, null) } returns convertedAudio
        every { transcriptionPort.transcribe(convertedAudio, "Portuguese") } returns transcription
        every { conversationRepository.save(any()) } returns savedConversation

        analyzeConversationService.analyze(
            audio = audio,
            audioFilename = null,
            topicTitle = "job interview",
            language = "Portuguese",
        )

        val conversationSlot = slot<Conversation>()
        verify { conversationRepository.save(capture(conversationSlot)) }
        assertEquals(
            "Consider expanding your vocabulary in Portuguese.",
            conversationSlot.captured.feedbackItems[0].recommendation,
        )
        assertEquals(
            "Practice verb tenses more in Portuguese.",
            conversationSlot.captured.feedbackItems[1].recommendation,
        )
    }

    @Test
    fun `analyze stores original audio and transcribes converted audio`() {
        val topicId = UUID.randomUUID()
        val topic =
            Topic(
                id = topicId,
                title = "daily routine",
                createdAt = OffsetDateTime.now(ZoneOffset.UTC),
                updatedAt = OffsetDateTime.now(ZoneOffset.UTC),
            )
        val originalAudio = byteArrayOf(1, 1, 1)
        val convertedAudio = byteArrayOf(2, 2, 2)
        val audioFilename = "recording.webm"
        val storageRef = AudioStorageReference("local:///tmp/audio/original.webm")
        val transcription = Transcription("Converted transcript")
        val savedConversation =
            Conversation(
                id = UUID.randomUUID(),
                topicId = topicId,
                audioStorageRef = storageRef.ref,
                transcript = transcription.text,
                feedbackItems = emptyList(),
                createdAt = OffsetDateTime.now(ZoneOffset.UTC),
                updatedAt = OffsetDateTime.now(ZoneOffset.UTC),
            )

        every { topicService.ensureTopic("daily routine") } returns topic
        every { audioStoragePort.store(originalAudio, audioFilename) } returns storageRef
        every { audioConversionPort.convert(originalAudio, audioFilename) } returns convertedAudio
        every { transcriptionPort.transcribe(convertedAudio, null) } returns transcription
        every { conversationRepository.save(any()) } returns savedConversation

        analyzeConversationService.analyze(
            audio = originalAudio,
            audioFilename = audioFilename,
            topicTitle = "daily routine",
            language = null,
        )

        verify { audioStoragePort.store(originalAudio, audioFilename) }
        verify { audioConversionPort.convert(originalAudio, audioFilename) }
        verify { transcriptionPort.transcribe(convertedAudio, null) }
    }
}
