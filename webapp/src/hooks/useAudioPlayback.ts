import { useCallback, useEffect, useRef, useState } from 'react'

export type PlaybackState = 'idle' | 'playing'

export interface AudioPlaybackValue {
  state: PlaybackState
  play: () => void
  stop: () => void
  reset: () => void
}

export function useAudioPlayback(blob: Blob | null): AudioPlaybackValue {
  const [state, setState] = useState<PlaybackState>('idle')
  const audioRef = useRef<HTMLAudioElement | null>(null)
  const urlRef = useRef<string | null>(null)

  const cleanup = useCallback(() => {
    const audio = audioRef.current
    if (audio) {
      audio.pause()
      audio.src = ''
      audio.load()
      audioRef.current = null
    }
    if (urlRef.current) {
      URL.revokeObjectURL(urlRef.current)
      urlRef.current = null
    }
    setState('idle')
  }, [])

  useEffect(() => {
    if (!blob) {
      cleanup()
      return
    }

    cleanup()

    const url = URL.createObjectURL(blob)
    urlRef.current = url

    const audio = new Audio(url)
    audioRef.current = audio

    const handlePlay = () => setState('playing')
    const handleEnded = () => setState('idle')
    const handlePause = () => setState('idle')
    const handleError = () => setState('idle')

    audio.addEventListener('play', handlePlay)
    audio.addEventListener('ended', handleEnded)
    audio.addEventListener('pause', handlePause)
    audio.addEventListener('error', handleError)

    return () => {
      audio.removeEventListener('play', handlePlay)
      audio.removeEventListener('ended', handleEnded)
      audio.removeEventListener('pause', handlePause)
      audio.removeEventListener('error', handleError)
      cleanup()
    }
  }, [blob, cleanup])

  const play = useCallback(() => {
    const audio = audioRef.current
    if (!audio) {
      setState('idle')
      return
    }

    audio.play().catch(() => {
      setState('idle')
    })
  }, [])

  const stop = useCallback(() => {
    const audio = audioRef.current
    if (!audio) {
      setState('idle')
      return
    }

    audio.pause()
    audio.currentTime = 0
    setState('idle')
  }, [])

  const reset = useCallback(() => {
    cleanup()
  }, [cleanup])

  useEffect(() => {
    return () => {
      cleanup()
    }
  }, [cleanup])

  return {
    state,
    play,
    stop,
    reset,
  }
}
