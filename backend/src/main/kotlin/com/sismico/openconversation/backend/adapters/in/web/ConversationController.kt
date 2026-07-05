package com.sismico.openconversation.backend.adapters.`in`.web

import com.sismico.openconversation.backend.adapters.`in`.web.dto.ConversationResponse
import com.sismico.openconversation.backend.adapters.`in`.web.dto.ConversationSummaryResponse
import com.sismico.openconversation.backend.adapters.`in`.web.dto.FeedbackItemResponse
import com.sismico.openconversation.backend.adapters.`in`.web.dto.PageResponse
import com.sismico.openconversation.backend.adapters.`in`.web.dto.PaginationMetaResponse
import com.sismico.openconversation.backend.adapters.`in`.web.error.NotFoundException
import com.sismico.openconversation.backend.application.ports.`in`.AnalyzeConversationUseCase
import com.sismico.openconversation.backend.application.ports.`in`.GetConversationUseCase
import com.sismico.openconversation.backend.application.ports.`in`.ListConversationsUseCase
import com.sismico.openconversation.backend.application.ports.out.persistence.ConversationFilters
import com.sismico.openconversation.backend.domain.ConversationSummary
import com.sismico.openconversation.backend.domain.ConversationWithTopic
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.UUID

@RestController
@RequestMapping("/api/v1/conversations")
class ConversationController(
    private val analyzeConversationUseCase: AnalyzeConversationUseCase,
    private val listConversationsUseCase: ListConversationsUseCase,
    private val getConversationUseCase: GetConversationUseCase,
) {
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun create(
        @RequestParam audio: MultipartFile,
        @RequestParam topicTitle: String,
    ): ResponseEntity<ConversationResponse> {
        require(!audio.isEmpty) { "Audio file must not be empty" }
        require(topicTitle.isNotBlank()) { "Topic title must not be blank" }

        val analyzed =
            analyzeConversationUseCase.analyze(
                audioStorageRef = "audio://placeholder/${UUID.randomUUID()}",
                topicTitle = topicTitle.trim(),
            )

        val location =
            ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(requireNotNull(analyzed.conversation.id))
                .toUri()

        return ResponseEntity
            .created(location)
            .body(analyzed.toResponse())
    }

    @GetMapping
    fun list(
        @RequestParam(required = false) topicId: UUID?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): PageResponse<ConversationSummaryResponse> {
        val filters = ConversationFilters(topicId = topicId)

        val result = listConversationsUseCase.list(filters, page, size)
        return PageResponse(
            data = result.data.map { it.toSummaryResponse() },
            pagination =
                PaginationMetaResponse(
                    page = result.page,
                    size = result.size,
                    totalElements = result.totalElements,
                    totalPages = result.totalPages,
                ),
        )
    }

    @GetMapping("/{id}")
    fun get(
        @PathVariable id: UUID,
    ): ConversationResponse {
        val conversation =
            getConversationUseCase.getById(id)
                ?: throw NotFoundException("Conversation not found: $id")

        return conversation.toResponse()
    }

    private fun ConversationSummary.toSummaryResponse(): ConversationSummaryResponse =
        ConversationSummaryResponse(
            id = id,
            topicId = topicId,
            topicTitle = topicTitle,
            audioStorageRef = audioStorageRef,
            createdAt = createdAt,
        )

    private fun ConversationWithTopic.toResponse(): ConversationResponse =
        ConversationResponse(
            id = requireNotNull(conversation.id),
            topicId = conversation.topicId,
            topicTitle = topicTitle,
            transcript = conversation.transcript,
            audioStorageRef = conversation.audioStorageRef,
            analyzedAt = conversation.analyzedAt,
            createdAt = conversation.createdAt,
            updatedAt = conversation.updatedAt,
            feedbackItems =
                conversation.feedbackItems.map {
                    FeedbackItemResponse(
                        id = requireNotNull(it.id),
                        excerpt = it.excerpt,
                        recommendation = it.recommendation,
                        displayOrder = it.displayOrder,
                    )
                },
        )
}
