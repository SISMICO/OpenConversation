import { act, renderHook } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { useAudioPlayback } from './useAudioPlayback'

class MockAudio {
  src = ''
  currentTime = 0
  readonly url: string
  private listeners: Record<string, EventListener[]> = {}

  constructor(url: string) {
    this.url = url
    this.src = url
  }

  addEventListener(event: string, handler: EventListener) {
    if (!this.listeners[event]) {
      this.listeners[event] = []
    }
    this.listeners[event].push(handler)
  }

  removeEventListener(event: string, handler: EventListener) {
    this.listeners[event] = this.listeners[event]?.filter((h) => h !== handler) ?? []
  }

  dispatchEvent(eventName: string) {
    const event = new Event(eventName)
    this.listeners[eventName]?.forEach((handler) => handler(event))
  }

  play = vi.fn(() => {
    this.dispatchEvent('play')
    return Promise.resolve()
  })

  pause = vi.fn(() => {
    this.dispatchEvent('pause')
  })

  load = vi.fn()
}

const originalAudio = (globalThis as { Audio?: typeof Audio }).Audio
const createdAudios: MockAudio[] = []

function createBlob(content = 'test'): Blob {
  return new Blob([content], { type: 'audio/webm' })
}

function getCurrentAudio(): MockAudio | undefined {
  return createdAudios[createdAudios.length - 1]
}

beforeEach(() => {
  createdAudios.length = 0

  vi.spyOn(URL, 'createObjectURL').mockImplementation(
    (blob) => `blob:${(blob as Blob).size}`,
  )
  vi.spyOn(URL, 'revokeObjectURL').mockImplementation(() => {})

  globalThis.Audio = function Audio(url: string) {
    const audio = new MockAudio(url)
    createdAudios.push(audio)
    return audio
  } as unknown as typeof Audio
})

afterEach(() => {
  vi.restoreAllMocks()
  ;(globalThis as { Audio?: typeof Audio }).Audio = originalAudio
})

describe('useAudioPlayback', () => {
  it('should return idle state when no blob is provided', () => {
    const { result } = renderHook(() => useAudioPlayback(null))

    expect(result.current.state).toBe('idle')
  })

  it('should call audio.play and transition to playing when play is called', () => {
    const blob = createBlob()
    const { result } = renderHook(() => useAudioPlayback(blob))

    act(() => {
      result.current.play()
    })

    expect(getCurrentAudio()?.play).toHaveBeenCalled()
    expect(result.current.state).toBe('playing')
  })

  it('should call audio.pause, reset currentTime, and return to idle when stop is called', () => {
    const blob = createBlob()
    const { result } = renderHook(() => useAudioPlayback(blob))

    act(() => {
      result.current.play()
    })
    act(() => {
      result.current.stop()
    })

    expect(getCurrentAudio()?.pause).toHaveBeenCalled()
    expect(getCurrentAudio()?.currentTime).toBe(0)
    expect(result.current.state).toBe('idle')
  })

  it('should return to idle when the audio ended event fires', () => {
    const blob = createBlob()
    const { result } = renderHook(() => useAudioPlayback(blob))

    act(() => {
      result.current.play()
    })
    act(() => {
      getCurrentAudio()?.dispatchEvent('ended')
    })

    expect(result.current.state).toBe('idle')
  })

  it('should return to idle when the audio error event fires', () => {
    const blob = createBlob()
    const { result } = renderHook(() => useAudioPlayback(blob))

    act(() => {
      result.current.play()
    })
    act(() => {
      getCurrentAudio()?.dispatchEvent('error')
    })

    expect(result.current.state).toBe('idle')
  })

  it('should revoke the old object URL and create a new one when the blob changes', () => {
    const blob1 = createBlob('first')
    const { rerender } = renderHook(({ blob }) => useAudioPlayback(blob), {
      initialProps: { blob: blob1 },
    })

    const firstUrl = `blob:${blob1.size}`
    expect(URL.createObjectURL).toHaveBeenCalledWith(blob1)

    const blob2 = createBlob('second')
    rerender({ blob: blob2 })

    expect(URL.revokeObjectURL).toHaveBeenCalledWith(firstUrl)
    expect(URL.createObjectURL).toHaveBeenCalledWith(blob2)
    expect(URL.createObjectURL).toHaveBeenCalledTimes(2)
  })

  it('should revoke the object URL and pause the audio on unmount', () => {
    const blob = createBlob()
    const { unmount } = renderHook(() => useAudioPlayback(blob))

    const url = `blob:${blob.size}`
    const audio = getCurrentAudio()

    unmount()

    expect(URL.revokeObjectURL).toHaveBeenCalledWith(url)
    expect(audio?.pause).toHaveBeenCalled()
  })

  it('should pause and clean up resources when reset is called', () => {
    const blob = createBlob()
    const { result } = renderHook(() => useAudioPlayback(blob))

    act(() => {
      result.current.play()
    })
    act(() => {
      result.current.reset()
    })

    expect(getCurrentAudio()?.pause).toHaveBeenCalled()
    expect(URL.revokeObjectURL).toHaveBeenCalled()
    expect(result.current.state).toBe('idle')
  })

  it('should remain idle when play is called without an audio element', () => {
    const { result } = renderHook(() => useAudioPlayback(null))

    act(() => {
      result.current.play()
    })

    expect(result.current.state).toBe('idle')
  })
})
