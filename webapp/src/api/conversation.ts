export interface FeedbackItem {
  id: string
  excerpt: string
  recommendation: string
  displayOrder: number
}

export interface ConversationResponse {
  id: string
  topicId: string
  topicTitle: string
  transcript: string
  audioStorageRef: string
  analyzedAt: string
  createdAt: string
  updatedAt: string
  feedbackItems: FeedbackItem[]
}

export interface CreateConversationRequest {
  audio: Blob
  topicTitle: string
}

export class ConversationError extends Error {
  constructor(message: string) {
    super(message)
    this.name = 'ConversationError'
  }
}

export async function createConversation(
  request: CreateConversationRequest,
): Promise<ConversationResponse> {
  const formData = new FormData()
  formData.append('audio', request.audio, 'recording')
  formData.append('topicTitle', request.topicTitle)

  let response: Response
  try {
    response = await fetch('/api/v1/conversations', {
      method: 'POST',
      body: formData,
    })
  } catch {
    throw new ConversationError(
      'Network error. Please check your connection and try again.',
    )
  }

  if (!response.ok) {
    throw new ConversationError(
      `Conversation request failed with status ${response.status}.`,
    )
  }

  try {
    const data = (await response.json()) as ConversationResponse
    if (
      typeof data.transcript !== 'string' ||
      !Array.isArray(data.feedbackItems)
    ) {
      throw new ConversationError('Invalid response from conversation service.')
    }
    return data
  } catch {
    throw new ConversationError('Invalid response from conversation service.')
  }
}
