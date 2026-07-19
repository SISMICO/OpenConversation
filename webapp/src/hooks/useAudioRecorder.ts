import { useCallback, useEffect, useRef, useState } from 'react'

export type RecorderState = 'idle' | 'recording' | 'paused' | 'stopped'

export type RecorderErrorType =
  | 'permission_denied'
  | 'no_microphone'
  | 'unsupported'
  | 'generic'

export interface AudioRecorderValue {
  state: RecorderState
  blob: Blob | null
  mimeType: string | null
  durationMs: number
  error: RecorderErrorType | null
}

export interface AudioRecorderActions {
  start: () => Promise<void>
  pause: () => void
  resume: () => void
  stop: () => void
  reset: () => void
  upload: (file: File) => Promise<void>
}

const PREFERRED_MIME_TYPE = 'audio/webm;codecs=opus'
const FALLBACK_MIME_TYPE = 'audio/mp4'

function getSupportedMimeType(): string {
  if (typeof MediaRecorder === 'undefined') {
    return ''
  }
  if (MediaRecorder.isTypeSupported(PREFERRED_MIME_TYPE)) {
    return PREFERRED_MIME_TYPE
  }
  if (MediaRecorder.isTypeSupported(FALLBACK_MIME_TYPE)) {
    return FALLBACK_MIME_TYPE
  }
  return ''
}

function classifyError(error: unknown): RecorderErrorType {
  if (error instanceof DOMException) {
    if (error.name === 'NotAllowedError' || error.name === 'PermissionDeniedError') {
      return 'permission_denied'
    }
    if (error.name === 'NotFoundError' || error.name === 'DevicesNotFoundError') {
      return 'no_microphone'
    }
    if (error.name === 'NotSupportedError') {
      return 'unsupported'
    }
  }
  return 'generic'
}

export function useAudioRecorder(): AudioRecorderValue & AudioRecorderActions {
  const [state, setState] = useState<RecorderState>('idle')
  const [blob, setBlob] = useState<Blob | null>(null)
  const [mimeType, setMimeType] = useState<string | null>(null)
  const [durationMs, setDurationMs] = useState(0)
  const [error, setError] = useState<RecorderErrorType | null>(null)

  const mediaRecorderRef = useRef<MediaRecorder | null>(null)
  const streamRef = useRef<MediaStream | null>(null)
  const chunksRef = useRef<Blob[]>([])
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null)
  const startTimeRef = useRef<number>(0)
  const accumulatedTimeRef = useRef<number>(0)
  const selectedMimeTypeRef = useRef<string>('')

  const clearTimer = useCallback(() => {
    if (timerRef.current) {
      clearInterval(timerRef.current)
      timerRef.current = null
    }
  }, [])

  const stopTracks = useCallback(() => {
    if (streamRef.current) {
      streamRef.current.getTracks().forEach((track) => track.stop())
      streamRef.current = null
    }
  }, [])

  const updateDuration = useCallback(() => {
    setDurationMs(accumulatedTimeRef.current + (Date.now() - startTimeRef.current))
  }, [])

  const startTimer = useCallback(() => {
    clearTimer()
    startTimeRef.current = Date.now()
    timerRef.current = setInterval(updateDuration, 100)
  }, [clearTimer, updateDuration])

  const pauseTimer = useCallback(() => {
    clearTimer()
    accumulatedTimeRef.current += Date.now() - startTimeRef.current
    setDurationMs(accumulatedTimeRef.current)
  }, [clearTimer])

  const reset = useCallback(() => {
    clearTimer()
    stopTracks()
    if (mediaRecorderRef.current?.state !== 'inactive') {
      try {
        mediaRecorderRef.current?.stop()
      } catch {
        // Recorder may already be stopped or inactive.
      }
    }
    mediaRecorderRef.current = null
    chunksRef.current = []
    startTimeRef.current = 0
    accumulatedTimeRef.current = 0
    selectedMimeTypeRef.current = ''
    setBlob(null)
    setMimeType(null)
    setDurationMs(0)
    setError(null)
    setState('idle')
  }, [clearTimer, stopTracks])

  const upload = useCallback(
    async (file: File) => {
      reset()

      if (!file) {
        setError('generic')
        setState('idle')
        return
      }

      try {
        setBlob(file)
        setMimeType(file.type || null)
        setDurationMs(0)
        setError(null)
        setState('stopped')
      } catch {
        setError('generic')
        setState('idle')
      }
    },
    [reset],
  )

  const start = useCallback(async () => {
    reset()

    if (typeof navigator === 'undefined' || !navigator.mediaDevices?.getUserMedia) {
      setError('unsupported')
      setState('idle')
      return
    }

    if (typeof MediaRecorder === 'undefined') {
      setError('unsupported')
      setState('idle')
      return
    }

    const selectedMimeType = getSupportedMimeType()
    if (!selectedMimeType) {
      setError('unsupported')
      setState('idle')
      return
    }
    selectedMimeTypeRef.current = selectedMimeType

    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
      streamRef.current = stream

      const recorder = new MediaRecorder(stream, { mimeType: selectedMimeType })
      mediaRecorderRef.current = recorder
      chunksRef.current = []

      recorder.ondataavailable = (event) => {
        if (event.data.size > 0) {
          chunksRef.current.push(event.data)
        }
      }

      recorder.onstop = () => {
        const recordedBlob = new Blob(chunksRef.current, { type: selectedMimeType })
        setBlob(recordedBlob)
        setMimeType(selectedMimeType)
        setState('stopped')
      }

      recorder.onerror = () => {
        setError('generic')
        setState('idle')
        stopTracks()
        clearTimer()
      }

      recorder.start(100)
      setError(null)
      setState('recording')
      startTimer()
    } catch (err) {
      stopTracks()
      setError(classifyError(err))
      setState('idle')
    }
  }, [reset, startTimer, stopTracks, clearTimer])

  const pause = useCallback(() => {
    const recorder = mediaRecorderRef.current
    if (recorder?.state === 'recording') {
      recorder.pause()
      pauseTimer()
      setState('paused')
    }
  }, [pauseTimer])

  const resume = useCallback(() => {
    const recorder = mediaRecorderRef.current
    if (recorder?.state === 'paused') {
      recorder.resume()
      startTimer()
      setState('recording')
    }
  }, [startTimer])

  const stop = useCallback(() => {
    const recorder = mediaRecorderRef.current
    if (recorder && recorder.state !== 'inactive') {
      recorder.stop()
    }
    stopTracks()
    clearTimer()
  }, [clearTimer, stopTracks])

  useEffect(() => {
    return () => {
      clearTimer()
      stopTracks()
      try {
        mediaRecorderRef.current?.stop()
      } catch {
        // Ignore cleanup errors.
      }
    }
  }, [clearTimer, stopTracks])

  return {
    state,
    blob,
    mimeType,
    durationMs,
    error,
    start,
    pause,
    resume,
    stop,
    reset,
    upload,
  }
}
