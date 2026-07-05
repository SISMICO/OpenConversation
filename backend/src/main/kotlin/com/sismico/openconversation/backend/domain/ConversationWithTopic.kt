package com.sismico.openconversation.backend.domain

data class ConversationWithTopic(
    val conversation: Conversation,
    val topicTitle: String,
)
