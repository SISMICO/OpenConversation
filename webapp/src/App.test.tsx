import { fireEvent, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import type { ReactNode } from 'react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import App from './App'
import { useAudioRecorder } from '#hooks/useAudioRecorder'
import { useAudioPlayback } from '#hooks/useAudioPlayback'
import { downloadBlob } from '#lib/utils'
import { createConversation } from '#api/conversation'
import type { AudioPlaybackValue } from '#hooks/useAudioPlayback'
import type { AudioRecorderActions, AudioRecorderValue } from '#hooks/useAudioRecorder'

type RecorderMock = AudioRecorderValue & AudioRecorderActions

type PlaybackMock = AudioPlaybackValue

vi.mock('next-themes', () => ({
  ThemeProvider: ({ children }: { children: ReactNode }) => children,
  useTheme: vi.fn(() => ({
    theme: 'light',
    resolvedTheme: 'light',
    systemTheme: 'light',
    themes: ['light', 'dark', 'system'],
    setTheme: vi.fn(),
    forcedTheme: undefined,
    setForcedTheme: vi.fn(),
  })),
}))

vi.mock('#hooks/useAudioRecorder', () => ({
  useAudioRecorder: vi.fn(),
}))

vi.mock('#hooks/useAudioPlayback', () => ({
  useAudioPlayback: vi.fn(),
}))

vi.mock('#api/conversation', () => ({
  createConversation: vi.fn(),
}))

vi.mock('#lib/utils', async (importOriginal) => {
  const actual = await importOriginal<typeof import('#lib/utils')>()
  return {
    ...actual,
    downloadBlob: vi.fn(),
    getAudioExtension: vi.fn((mimeType: string | null) => {
      if (mimeType?.toLowerCase().startsWith('audio/webm')) {
        return '.webm'
      }
      if (mimeType?.toLowerCase().startsWith('audio/mp4')) {
        return '.mp4'
      }
      return '.bin'
    }),
  }
})

const mockedUseAudioRecorder = vi.mocked(useAudioRecorder)
const mockedUseAudioPlayback = vi.mocked(useAudioPlayback)
const mockedDownloadBlob = vi.mocked(downloadBlob)
const mockedCreateConversation = vi.mocked(createConversation)

function createRecorderMock(overrides: Partial<RecorderMock> = {}): RecorderMock {
  return {
    state: 'idle',
    blob: null,
    mimeType: null,
    durationMs: 0,
    error: null,
    start: vi.fn(),
    pause: vi.fn(),
    resume: vi.fn(),
    stop: vi.fn(),
    reset: vi.fn(),
    upload: vi.fn(),
    ...overrides,
  }
}

function createPlaybackMock(overrides: Partial<PlaybackMock> = {}): PlaybackMock {
  return {
    state: 'idle',
    play: vi.fn(),
    stop: vi.fn(),
    reset: vi.fn(),
    ...overrides,
  }
}

beforeEach(() => {
  mockedUseAudioRecorder.mockReturnValue(createRecorderMock())
  mockedUseAudioPlayback.mockReturnValue(createPlaybackMock())
})

afterEach(() => {
  vi.restoreAllMocks()
})

describe('App audio playback integration', () => {
  it('should not show the Play button while recording', () => {
    mockedUseAudioRecorder.mockReturnValue(createRecorderMock({ state: 'recording' }))

    render(<App />)

    expect(screen.queryByRole('button', { name: 'Play' })).not.toBeInTheDocument()
  })

  it('should show the Play button after stopping a recording', () => {
    mockedUseAudioRecorder.mockReturnValue(
      createRecorderMock({ state: 'stopped', blob: new Blob(['audio']) }),
    )
    mockedUseAudioPlayback.mockReturnValue(createPlaybackMock())

    render(<App />)

    expect(screen.getByRole('button', { name: 'Play' })).toBeInTheDocument()
  })

  it('should call playback.play when the Play button is clicked', async () => {
    const user = userEvent.setup()
    const playback = createPlaybackMock()

    mockedUseAudioRecorder.mockReturnValue(
      createRecorderMock({ state: 'stopped', blob: new Blob(['audio']) }),
    )
    mockedUseAudioPlayback.mockReturnValue(playback)

    render(<App />)

    const playButton = screen.getByRole('button', { name: 'Play' })
    await user.click(playButton)

    expect(playback.play).toHaveBeenCalled()
  })

  it('should call playback.stop when the Stop button is clicked during playback', async () => {
    const user = userEvent.setup()
    const playback = createPlaybackMock({ state: 'playing' })

    mockedUseAudioRecorder.mockReturnValue(
      createRecorderMock({ state: 'stopped', blob: new Blob(['audio']) }),
    )
    mockedUseAudioPlayback.mockReturnValue(playback)

    render(<App />)

    const stopButton = screen.getByRole('button', { name: 'Stop' })
    await user.click(stopButton)

    expect(playback.stop).toHaveBeenCalled()
  })

  it('should keep Send and Discard buttons available after stopping a recording', () => {
    mockedUseAudioRecorder.mockReturnValue(
      createRecorderMock({ state: 'stopped', blob: new Blob(['audio']) }),
    )
    mockedUseAudioPlayback.mockReturnValue(createPlaybackMock())

    render(<App />)

    expect(screen.getByRole('button', { name: 'Send' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Discard' })).toBeInTheDocument()
  })

  it('should reset playback when starting a new recording', async () => {
    const user = userEvent.setup()
    const playback = createPlaybackMock()
    const recorder = createRecorderMock()

    mockedUseAudioRecorder.mockReturnValue(recorder)
    mockedUseAudioPlayback.mockReturnValue(playback)

    render(<App />)

    const startButton = screen.getByRole('button', { name: 'Start' })
    await user.click(startButton)

    expect(playback.reset).toHaveBeenCalled()
    expect(recorder.start).toHaveBeenCalled()
  })

  it('should reset playback and recorder when discarding a recording', async () => {
    const user = userEvent.setup()
    const playback = createPlaybackMock()
    const recorder = createRecorderMock({
      state: 'stopped',
      blob: new Blob(['audio']),
    })

    mockedUseAudioRecorder.mockReturnValue(recorder)
    mockedUseAudioPlayback.mockReturnValue(playback)

    render(<App />)

    const discardTrigger = screen.getByRole('button', { name: 'Discard' })
    await user.click(discardTrigger)

    const discardButtons = screen.getAllByRole('button', { name: 'Discard' })
    await user.click(discardButtons[discardButtons.length - 1])

    expect(playback.reset).toHaveBeenCalled()
    expect(recorder.reset).toHaveBeenCalled()
  })

  it('should not show the Download button while recording', () => {
    mockedUseAudioRecorder.mockReturnValue(createRecorderMock({ state: 'recording' }))

    render(<App />)

    expect(screen.queryByRole('button', { name: 'Download' })).not.toBeInTheDocument()
  })

  it('should not show the Download button while paused', () => {
    mockedUseAudioRecorder.mockReturnValue(createRecorderMock({ state: 'paused' }))

    render(<App />)

    expect(screen.queryByRole('button', { name: 'Download' })).not.toBeInTheDocument()
  })

  it('should show the Download button after stopping a recording', () => {
    mockedUseAudioRecorder.mockReturnValue(
      createRecorderMock({ state: 'stopped', blob: new Blob(['audio']) }),
    )

    render(<App />)

    expect(screen.getByRole('button', { name: 'Download' })).toBeInTheDocument()
  })

  it('should disable the Download button when stopped but no blob exists', () => {
    mockedUseAudioRecorder.mockReturnValue(createRecorderMock({ state: 'stopped' }))

    render(<App />)

    expect(screen.getByRole('button', { name: 'Download' })).toBeDisabled()
  })

  it('should call downloadBlob with the recorded blob and webm filename', async () => {
    const user = userEvent.setup()
    const blob = new Blob(['audio'], { type: 'audio/webm;codecs=opus' })

    mockedUseAudioRecorder.mockReturnValue(
      createRecorderMock({ state: 'stopped', blob, mimeType: 'audio/webm;codecs=opus' }),
    )

    render(<App />)

    const downloadButton = screen.getByRole('button', { name: 'Download' })
    await user.click(downloadButton)

    expect(mockedDownloadBlob).toHaveBeenCalledWith(blob, 'recording.webm')
  })

  it('should call downloadBlob with the recorded blob and mp4 filename', async () => {
    const user = userEvent.setup()
    const blob = new Blob(['audio'], { type: 'audio/mp4' })

    mockedUseAudioRecorder.mockReturnValue(
      createRecorderMock({ state: 'stopped', blob, mimeType: 'audio/mp4' }),
    )

    render(<App />)

    const downloadButton = screen.getByRole('button', { name: 'Download' })
    await user.click(downloadButton)

    expect(mockedDownloadBlob).toHaveBeenCalledWith(blob, 'recording.mp4')
  })

  it('should call downloadBlob with the recorded blob and bin filename for unknown MIME type', async () => {
    const user = userEvent.setup()
    const blob = new Blob(['audio'], { type: 'audio/ogg' })

    mockedUseAudioRecorder.mockReturnValue(
      createRecorderMock({ state: 'stopped', blob, mimeType: 'audio/ogg' }),
    )

    render(<App />)

    const downloadButton = screen.getByRole('button', { name: 'Download' })
    await user.click(downloadButton)

    expect(mockedDownloadBlob).toHaveBeenCalledWith(blob, 'recording.bin')
  })

  it('should keep Play, Send, and Discard available after clicking Download', async () => {
    const user = userEvent.setup()
    const playback = createPlaybackMock()
    const recorder = createRecorderMock({
      state: 'stopped',
      blob: new Blob(['audio']),
    })

    mockedUseAudioRecorder.mockReturnValue(recorder)
    mockedUseAudioPlayback.mockReturnValue(playback)

    render(<App />)

    const downloadButton = screen.getByRole('button', { name: 'Download' })
    await user.click(downloadButton)

    expect(mockedDownloadBlob).toHaveBeenCalled()
    expect(recorder.reset).not.toHaveBeenCalled()
    expect(playback.reset).not.toHaveBeenCalled()
    expect(screen.getByRole('button', { name: 'Play' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Send' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Discard' })).toBeInTheDocument()
  })

  it('should hide the Download button while sending', async () => {
    const user = userEvent.setup()
    const recorder = createRecorderMock({
      state: 'stopped',
      blob: new Blob(['audio']),
    })

    mockedUseAudioRecorder.mockReturnValue(recorder)
    mockedCreateConversation.mockReturnValue(new Promise(() => {}))

    render(<App />)

    const topicInput = screen.getByPlaceholderText('Simulate a job interview about your career and describe your last role.')
    await user.type(topicInput, 'Job interview practice')

    const sendButton = screen.getByRole('button', { name: 'Send' })
    await user.click(sendButton)

    expect(screen.queryByRole('button', { name: 'Download' })).not.toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Analysing...' })).toBeInTheDocument()
  })
})

describe('App audio upload integration', () => {
  function createAudioFile(name = 'test.webm', type = 'audio/webm'): File {
    return new File(['audio content'], name, { type })
  }

  it('should render the Upload button in idle state', () => {
    mockedUseAudioRecorder.mockReturnValue(createRecorderMock({ state: 'idle' }))

    render(<App />)

    expect(screen.getByRole('button', { name: 'Upload' })).toBeInTheDocument()
  })

  it('should hide the Upload button outside idle state', () => {
    const states: Array<'recording' | 'paused' | 'stopped'> = ['recording', 'paused', 'stopped']

    for (const state of states) {
      mockedUseAudioRecorder.mockReturnValue(createRecorderMock({ state }))
      const { unmount } = render(<App />)

      expect(screen.queryByRole('button', { name: 'Upload' })).not.toBeInTheDocument()
      unmount()
    }
  })

  it('should have an accessible Upload button', () => {
    mockedUseAudioRecorder.mockReturnValue(createRecorderMock({ state: 'idle' }))

    render(<App />)

    expect(screen.getByRole('button', { name: 'Upload' })).toHaveAccessibleName('Upload')
  })

  it('should trigger the hidden file input when Upload is clicked', async () => {
    const user = userEvent.setup()
    const clickSpy = vi.spyOn(HTMLInputElement.prototype, 'click')

    mockedUseAudioRecorder.mockReturnValue(createRecorderMock({ state: 'idle' }))

    render(<App />)

    const uploadButton = screen.getByRole('button', { name: 'Upload' })
    await user.click(uploadButton)

    expect(clickSpy).toHaveBeenCalled()
  })

  it('should call recorder.upload when a file is selected', async () => {
    const upload = vi.fn()
    mockedUseAudioRecorder.mockReturnValue(createRecorderMock({ state: 'idle', upload }))

    render(<App />)

    const fileInput = screen.getByLabelText('Upload audio file')
    const file = createAudioFile()

    fireEvent.change(fileInput, { target: { files: [file] } })

    expect(upload).toHaveBeenCalledTimes(1)
    expect(upload).toHaveBeenCalledWith(file)
  })

  it('should clear previous feedback when a file is selected', async () => {
    const user = userEvent.setup()
    const blob = new Blob(['audio'])
    const recorder = createRecorderMock({ state: 'stopped', blob })

    mockedUseAudioRecorder.mockReturnValue(recorder)
    mockedCreateConversation.mockResolvedValue({
      id: 'conv-1',
      topicId: 'topic-1',
      topicTitle: 'Job interview practice',
      transcript: 'Test transcript',
      audioStorageRef: 'local:///app/audios/test.webm',
      analyzedAt: new Date().toISOString(),
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      feedbackItems: [
        {
          id: 'fb-1',
          excerpt: 'A phrase',
          recommendation: 'A recommendation',
          displayOrder: 0,
        },
      ],
    })

    render(<App />)

    const topicInput = screen.getByPlaceholderText('Simulate a job interview about your career and describe your last role.')
    await user.type(topicInput, 'Job interview practice')

    const sendButton = screen.getByRole('button', { name: 'Send' })
    await user.click(sendButton)

    await screen.findByText('Test transcript')

    const fileInput = screen.getByLabelText('Upload audio file')
    fireEvent.change(fileInput, { target: { files: [createAudioFile()] } })

    expect(recorder.upload).toHaveBeenCalled()
    expect(screen.getByText('Your feedback will appear here after you send a recording.')).toBeInTheDocument()
  })

  it('should clear previous send error when a file is selected', async () => {
    const user = userEvent.setup()
    const blob = new Blob(['audio'])
    const recorder = createRecorderMock({ state: 'stopped', blob })

    mockedUseAudioRecorder.mockReturnValue(recorder)
    mockedCreateConversation.mockRejectedValue(
      new Error('Network error. Please check your connection and try again.'),
    )

    render(<App />)

    const topicInput = screen.getByPlaceholderText('Simulate a job interview about your career and describe your last role.')
    await user.type(topicInput, 'Job interview practice')

    const sendButton = screen.getByRole('button', { name: 'Send' })
    await user.click(sendButton)

    await screen.findByText('Network error. Please check your connection and try again.')

    const fileInput = screen.getByLabelText('Upload audio file')
    fireEvent.change(fileInput, { target: { files: [createAudioFile()] } })

    expect(recorder.upload).toHaveBeenCalled()
    expect(screen.queryByText('Network error. Please check your connection and try again.')).not.toBeInTheDocument()
  })

  it('should show Play, Send, and Discard after an upload', () => {
    mockedUseAudioRecorder.mockReturnValue(
      createRecorderMock({ state: 'stopped', blob: new Blob(['audio']) }),
    )

    render(<App />)

    expect(screen.getByRole('button', { name: 'Play' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Send' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Discard' })).toBeInTheDocument()
  })

  it('should disable Send when no topic title is provided after an upload', () => {
    mockedUseAudioRecorder.mockReturnValue(
      createRecorderMock({ state: 'stopped', blob: new Blob(['audio']) }),
    )

    render(<App />)

    expect(screen.getByRole('button', { name: 'Send' })).toBeDisabled()
  })

  it('should hide the Start button after an upload', () => {
    mockedUseAudioRecorder.mockReturnValue(
      createRecorderMock({ state: 'stopped', blob: new Blob(['audio']) }),
    )

    render(<App />)

    expect(screen.queryByRole('button', { name: 'Start' })).not.toBeInTheDocument()
  })
})
