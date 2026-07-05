import { useCallback, useState } from 'react'
import {
  AlertCircle,
  Loader2,
  Mic,
  Pause,
  Play,
  Send,
  Square,
  Trash2,
} from 'lucide-react'
import { Header } from '#components/Header'
import { Button } from '#components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '#components/ui/card'
import {
  Alert,
  AlertDescription,
  AlertTitle,
} from '#components/ui/alert'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '#components/ui/dialog'
import { useAudioRecorder, type RecorderErrorType } from '#hooks/useAudioRecorder'
import { analyseAudio } from '#api/analysis'
import './App.css'

const TOPIC_PLACEHOLDER =
  'Simulate a job interview about your career and describe your last role.'

const ERROR_MESSAGES: Record<RecorderErrorType, string> = {
  permission_denied: 'Please allow microphone access to record your speech.',
  no_microphone: 'No microphone was found on your device.',
  unsupported: "Your browser doesn't support audio recording.",
  generic: 'Something went wrong. Please try again.',
}

function formatDuration(ms: number): string {
  const totalSeconds = Math.floor(ms / 1000)
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
}

export default function App() {
  const recorder = useAudioRecorder()
  const [feedback, setFeedback] = useState<string | null>(null)
  const [isSending, setIsSending] = useState(false)
  const [sendError, setSendError] = useState<string | null>(null)
  const [isDiscardOpen, setIsDiscardOpen] = useState(false)

  const handleStart = useCallback(async () => {
    setFeedback(null)
    setSendError(null)
    await recorder.start()
  }, [recorder])

  const handleSend = useCallback(async () => {
    if (!recorder.blob || !recorder.mimeType) {
      return
    }

    setIsSending(true)
    setSendError(null)

    try {
      const result = await analyseAudio({
        audio: recorder.blob,
        mimeType: recorder.mimeType,
        language: 'en',
      })
      setFeedback(result.feedback)
    } catch (error) {
      setSendError(
        error instanceof Error
          ? error.message
          : 'Something went wrong. Please try again.',
      )
    } finally {
      setIsSending(false)
    }
  }, [recorder.blob, recorder.mimeType])

  const handleDiscard = useCallback(() => {
    recorder.reset()
    setFeedback(null)
    setSendError(null)
    setIsDiscardOpen(false)
  }, [recorder])

  const recorderError = recorder.error ? ERROR_MESSAGES[recorder.error] : null

  return (
    <div className="app-container">
      <Header className="mb-6" />
      <main className="main-content">
        <Card className="topic-card">
          <CardHeader>
            <CardTitle>Topic</CardTitle>
            <CardDescription>Practice speaking about this topic</CardDescription>
          </CardHeader>
          <CardContent>
            <p className="topic-text">{TOPIC_PLACEHOLDER}</p>
          </CardContent>
        </Card>

        <Card className="controls-card">
          <CardContent className="controls-content">
            <div className="timer" aria-live="polite">
              {formatDuration(recorder.durationMs)}
            </div>

            <div className="control-buttons">
              {recorder.state === 'idle' && (
                <Button
                  onClick={handleStart}
                  size="lg"
                  className="control-button"
                >
                  <Mic className="mr-2 size-5" />
                  Start
                </Button>
              )}

              {recorder.state === 'recording' && (
                <>
                  <Button
                    onClick={recorder.pause}
                    variant="secondary"
                    size="lg"
                    className="control-button"
                  >
                    <Pause className="mr-2 size-5" />
                    Pause
                  </Button>
                  <Button
                    onClick={recorder.stop}
                    variant="destructive"
                    size="lg"
                    className="control-button"
                  >
                    <Square className="mr-2 size-5" />
                    Stop
                  </Button>
                </>
              )}

              {recorder.state === 'paused' && (
                <>
                  <Button
                    onClick={recorder.resume}
                    size="lg"
                    className="control-button"
                  >
                    <Play className="mr-2 size-5" />
                    Resume
                  </Button>
                  <Button
                    onClick={recorder.stop}
                    variant="destructive"
                    size="lg"
                    className="control-button"
                  >
                    <Square className="mr-2 size-5" />
                    Stop
                  </Button>
                </>
              )}

              {recorder.state === 'stopped' && !isSending && (
                <>
                  <Button
                    onClick={handleSend}
                    size="lg"
                    className="control-button"
                  >
                    <Send className="mr-2 size-5" />
                    Send
                  </Button>
                  <Dialog
                    open={isDiscardOpen}
                    onOpenChange={setIsDiscardOpen}
                  >
                    <DialogTrigger asChild>
                      <Button
                        variant="outline"
                        size="lg"
                        className="control-button"
                      >
                        <Trash2 className="mr-2 size-5" />
                        Discard
                      </Button>
                    </DialogTrigger>
                    <DialogContent>
                      <DialogHeader>
                        <DialogTitle>Discard recording?</DialogTitle>
                        <DialogDescription>
                          This will delete your current recording. This action
                          cannot be undone.
                        </DialogDescription>
                      </DialogHeader>
                      <DialogFooter>
                        <Button
                          variant="outline"
                          onClick={() => setIsDiscardOpen(false)}
                        >
                          Cancel
                        </Button>
                        <Button
                          variant="destructive"
                          onClick={handleDiscard}
                        >
                          Discard
                        </Button>
                      </DialogFooter>
                    </DialogContent>
                  </Dialog>
                </>
              )}

              {isSending && (
                <Button
                  disabled
                  size="lg"
                  className="control-button"
                >
                  <Loader2 className="mr-2 size-5 animate-spin" />
                  Analysing...
                </Button>
              )}
            </div>

            {recorderError && (
              <Alert
                variant="destructive"
                className="error-alert"
              >
                <AlertCircle className="size-4" />
                <AlertTitle>Error</AlertTitle>
                <AlertDescription>{recorderError}</AlertDescription>
              </Alert>
            )}

            {sendError && (
              <Alert
                variant="destructive"
                className="error-alert"
              >
                <AlertCircle className="size-4" />
                <AlertTitle>Error</AlertTitle>
                <AlertDescription>{sendError}</AlertDescription>
              </Alert>
            )}
          </CardContent>
        </Card>

        <Card className="feedback-card">
          <CardHeader>
            <CardTitle>Feedback</CardTitle>
          </CardHeader>
          <CardContent>
            {isSending ? (
              <div className="loading-state">
                <Loader2 className="size-8 animate-spin" />
                <p>Analysing your recording...</p>
              </div>
            ) : feedback ? (
              <p className="feedback-text">{feedback}</p>
            ) : (
              <p className="feedback-placeholder">
                Your feedback will appear here after you send a recording.
              </p>
            )}
          </CardContent>
        </Card>
      </main>
    </div>
  )
}
