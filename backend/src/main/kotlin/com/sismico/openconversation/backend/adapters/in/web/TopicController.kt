package com.sismico.openconversation.backend.adapters.`in`.web

import com.sismico.openconversation.backend.adapters.`in`.web.dto.ConversationFeedbackResponse
import com.sismico.openconversation.backend.adapters.`in`.web.dto.FeedbackItemResponse
import com.sismico.openconversation.backend.adapters.`in`.web.dto.PageResponse
import com.sismico.openconversation.backend.adapters.`in`.web.dto.PaginationMetaResponse
import com.sismico.openconversation.backend.adapters.`in`.web.dto.TopicResponse
import com.sismico.openconversation.backend.application.ports.`in`.GetTopicFeedbackUseCase
import com.sismico.openconversation.backend.application.ports.`in`.SearchTopicsUseCase
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import com.sismico.openconversation.backend.domain.ConversationFeedbackGroup as DomainConversationFeedbackGroup
import com.sismico.openconversation.backend.domain.Topic as DomainTopic

@RestController
@RequestMapping("/api/v1/topics")
class TopicController(
    private val searchTopicsUseCase: SearchTopicsUseCase,
    private val getTopicFeedbackUseCase: GetTopicFeedbackUseCase,
) {
    @GetMapping
    fun search(
        @RequestParam(required = false) q: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): PageResponse<TopicResponse> {
        val result = searchTopicsUseCase.search(q, page, size)
        return PageResponse(
            data = result.data.map { it.toResponse() },
            pagination =
                PaginationMetaResponse(
                    page = result.page,
                    size = result.size,
                    totalElements = result.totalElements,
                    totalPages = result.totalPages,
                ),
        )
    }

    @GetMapping("/{id}/feedback")
    fun getFeedback(
        @PathVariable id: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): PageResponse<ConversationFeedbackResponse> {
        val result = getTopicFeedbackUseCase.getFeedbackByTopicId(id, page, size)
        return PageResponse(
            data = result.data.map { it.toResponse() },
            pagination =
                PaginationMetaResponse(
                    page = result.page,
                    size = result.size,
                    totalElements = result.totalElements,
                    totalPages = result.totalPages,
                ),
        )
    }

    private fun DomainTopic.toResponse(): TopicResponse =
        TopicResponse(
            id = requireNotNull(id),
            title = title,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    private fun DomainConversationFeedbackGroup.toResponse(): ConversationFeedbackResponse =
        ConversationFeedbackResponse(
            conversationId = conversationId,
            createdAt = createdAt,
            feedbackItems =
                feedbackItems.map {
                    FeedbackItemResponse(
                        id = requireNotNull(it.id),
                        excerpt = it.excerpt,
                        recommendation = it.recommendation,
                        displayOrder = it.displayOrder,
                    )
                },
        )
}
