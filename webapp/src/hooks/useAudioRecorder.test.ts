import { act, renderHook } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { useAudioRecorder } from './useAudioRecorder'

class MockMediaRecorder {
  static isTypeSupported = vi.fn(() => true)

  state = 'inactive'
  ondataavailable: ((event: { data: Blob }) => void) | null = null
  onstop: (() => void) | null = null
  onerror: (() => void) | null = null

  start() {
    this.state = 'recording'
  }

  stop() {
    if (this.state !== 'inactive') {
      this.state = 'inactive'
      this.onstop?.()
    }
  }

  pause() {
    this.state = 'paused'
  }

  resume() {
    this.state = 'recording'
  }
}

const originalNavigator = globalThis.navigator
const originalMediaRecorder = globalThis.MediaRecorder

describe('useAudioRecorder upload', () => {
  let getUserMediaMock: ReturnType<typeof vi.fn>
  let stopSpy: ReturnType<typeof vi.spyOn>

  beforeEach(() => {
    stopSpy = vi.spyOn(MockMediaRecorder.prototype, 'stop')

    getUserMediaMock = vi.fn(() =>
      Promise.resolve({
        getTracks: () => [{ stop: vi.fn() }],
      }),
    )

    vi.stubGlobal('navigator', {
      mediaDevices: {
        getUserMedia: getUserMediaMock,
      },
    })

    vi.stubGlobal(
      'MediaRecorder',
      MockMediaRecorder as unknown as typeof MediaRecorder,
    )
  })

  afterEach(() => {
    vi.restoreAllMocks()
    globalThis.navigator = originalNavigator
    globalThis.MediaRecorder = originalMediaRecorder
  })

  function createAudioFile(
    name = 'test.webm',
    type = 'audio/webm',
  ): File {
    return new File(['audio content'], name, { type })
  }

  it('should transition to stopped state with file metadata after upload', async () => {
    const { result } = renderHook(() => useAudioRecorder())
    const file = createAudioFile()

    await act(async () => {
      await result.current.upload(file)
    })

    expect(result.current.state).toBe('stopped')
    expect(result.current.blob).toBe(file)
    expect(result.current.mimeType).toBe('audio/webm')
    expect(result.current.durationMs).toBe(0)
    expect(result.current.error).toBeNull()
  })

  it('should reset an active recording before loading the uploaded file', async () => {
    const { result } = renderHook(() => useAudioRecorder())

    await act(async () => {
      await result.current.start()
    })

    expect(result.current.state).toBe('recording')

    const file = createAudioFile('uploaded.mp3', 'audio/mp3')

    await act(async () => {
      await result.current.upload(file)
    })

    expect(stopSpy).toHaveBeenCalled()
    expect(result.current.state).toBe('stopped')
    expect(result.current.blob).toBe(file)
    expect(result.current.mimeType).toBe('audio/mp3')
  })

  it('should clear a previous error when upload succeeds', async () => {
    getUserMediaMock.mockRejectedValueOnce(new Error('denied'))

    const { result } = renderHook(() => useAudioRecorder())

    await act(async () => {
      await result.current.start()
    })

    expect(result.current.error).not.toBeNull()

    const file = createAudioFile()

    await act(async () => {
      await result.current.upload(file)
    })

    expect(result.current.error).toBeNull()
    expect(result.current.state).toBe('stopped')
    expect(result.current.blob).toBe(file)
  })

  it('should be a no-op when upload is called without a file', async () => {
    const { result } = renderHook(() => useAudioRecorder())

    await act(async () => {
      await result.current.upload(null as unknown as File)
    })

    expect(result.current.state).toBe('idle')
    expect(result.current.blob).toBeNull()
    expect(result.current.mimeType).toBeNull()
    expect(result.current.durationMs).toBe(0)
  })
})
