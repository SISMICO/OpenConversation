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
import { Input } from '#components/ui/input'
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
import { useAudioPlayback } from '#hooks/useAudioPlayback'
import {
  createConversation,
  type ConversationResponse,
} from '#api/conversation'
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
  const playback = useAudioPlayback(recorder.blob)
  const [topicTitle, setTopicTitle] = useState('')
  const [conversation, setConversation] = useState<ConversationResponse | null>(
    null,
  )
  const [isSending, setIsSending] = useState(false)
  const [sendError, setSendError] = useState<string | null>(null)
  const [isDiscardOpen, setIsDiscardOpen] = useState(false)

  const handleStart = useCallback(async () => {
    setConversation(null)
    setSendError(null)
    playback.reset()
    await recorder.start()
  }, [recorder, playback])

  const handleSend = useCallback(async () => {
    if (!recorder.blob || !topicTitle.trim()) {
      return
    }

    setIsSending(true)
    setSendError(null)

    try {
      const result = await createConversation({
        audio: recorder.blob,
        topicTitle: topicTitle.trim(),
      })
      setConversation(result)
    } catch (error) {
      setSendError(
        error instanceof Error
          ? error.message
          : 'Something went wrong. Please try again.',
      )
    } finally {
      setIsSending(false)
    }
  }, [recorder.blob, topicTitle])

  const handleDiscard = useCallback(() => {
    playback.reset()
    recorder.reset()
    setConversation(null)
    setSendError(null)
    setIsDiscardOpen(false)
  }, [recorder, playback])

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
            <label
              htmlFor="topic-title"
              className="mb-2 block text-sm font-medium text-foreground"
            >
              What would you like to talk about?
            </label>
            <Input
              id="topic-title"
              value={topicTitle}
              onChange={(event) => setTopicTitle(event.target.value)}
              placeholder={TOPIC_PLACEHOLDER}
              disabled={recorder.state === 'recording'}
            />
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
                    onClick={playback.state === 'playing' ? playback.stop : playback.play}
                    variant="secondary"
                    size="lg"
                    className="control-button"
                    disabled={!recorder.blob}
                  >
                    {playback.state === 'playing' ? (
                      <>
                        <Square className="mr-2 size-5" />
                        Stop
                      </>
                    ) : (
                      <>
                        <Play className="mr-2 size-5" />
                        Play
                      </>
                    )}
                  </Button>
                  <Button
                    onClick={handleSend}
                    size="lg"
                    className="control-button"
                    disabled={!recorder.blob || !topicTitle.trim()}
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
            ) : conversation ? (
              <div className="space-y-6">
                <section>
                  <h3 className="mb-2 text-sm font-semibold uppercase tracking-wide text-muted-foreground">
                    Transcript
                  </h3>
                  <p className="feedback-text">{conversation.transcript}</p>
                </section>
                <section>
                  <h3 className="mb-2 text-sm font-semibold uppercase tracking-wide text-muted-foreground">
                    Feedback
                  </h3>
                  {conversation.feedbackItems.length > 0 ? (
                    <ul className="feedback-list space-y-4">
                      {conversation.feedbackItems.map((item) => (
                        <li
                          key={item.id}
                          className="feedback-item"
                        >
                          <p className="feedback-excerpt">
                            &ldquo;{item.excerpt}&rdquo;
                          </p>
                          <p className="feedback-recommendation">
                            {item.recommendation}
                          </p>
                        </li>
                      ))}
                    </ul>
                  ) : (
                    <p className="feedback-placeholder">
                      No specific feedback items were returned for this
                      recording.
                    </p>
                  )}
                </section>
              </div>
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
