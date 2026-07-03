export interface AnalysisRequest {
  audio: Blob
  mimeType: string
  language: string
}

export interface AnalysisResponse {
  feedback: string
}

export class AnalysisError extends Error {
  constructor(message: string) {
    super(message)
    this.name = 'AnalysisError'
  }
}

export async function analyseAudio(request: AnalysisRequest): Promise<AnalysisResponse> {
  const formData = new FormData()
  formData.append('audio', request.audio, 'recording')
  formData.append('mimeType', request.mimeType)
  formData.append('language', request.language)

  let response: Response
  try {
    response = await fetch('/api/analyse', {
      method: 'POST',
      body: formData,
    })
  } catch {
    throw new AnalysisError('Network error. Please check your connection and try again.')
  }

  if (!response.ok) {
    throw new AnalysisError(`Analysis request failed with status ${response.status}.`)
  }

  try {
    const data = (await response.json()) as AnalysisResponse
    if (typeof data.feedback !== 'string') {
      throw new AnalysisError('Invalid response from analysis service.')
    }
    return data
  } catch {
    throw new AnalysisError('Invalid response from analysis service.')
  }
}
