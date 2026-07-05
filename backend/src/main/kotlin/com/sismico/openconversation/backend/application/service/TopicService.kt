package com.sismico.openconversation.backend.application.service

import com.sismico.openconversation.backend.application.ports.out.persistence.TopicRepositoryPort
import com.sismico.openconversation.backend.domain.Topic
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class TopicService(
    private val topicRepository: TopicRepositoryPort,
) {
    @Transactional
    fun ensureTopic(title: String): Topic {
        require(title.isNotBlank()) { "Topic title must not be blank" }

        val normalizedTitle = title.trim()
        val existing =
            topicRepository
                .searchByTitle(normalizedTitle)
                .firstOrNull { it.title.equals(normalizedTitle, ignoreCase = true) }

        return existing ?: topicRepository.save(
            Topic(title = normalizedTitle),
        )
    }
}
