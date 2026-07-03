import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import type { ViteDevServer, Connect } from 'vite'
import type { IncomingMessage, ServerResponse } from 'node:http'

const MOCK_DELAY_MS = 10_000

const FEEDBACK_OPTIONS = [
  'Great job! Your pronunciation was clear and your vocabulary use was natural.',
  'Nice work. Try to use a few more complex sentence structures next time.',
  'Good effort. Focus on speaking at a steady pace and reducing filler words.',
  'Well done! Your grammar was mostly accurate and your ideas were easy to follow.',
  'Keep practicing. Recording yourself regularly is the best way to improve fluency.',
]

function consumeRequestBody(req: IncomingMessage): Promise<Buffer> {
  return new Promise((resolve) => {
    const chunks: Buffer[] = []
    req.on('data', (chunk: Buffer) => chunks.push(chunk))
    req.on('end', () => resolve(Buffer.concat(chunks)))
  })
}

function mockAnalysisApiMiddleware() {
  return async (
    req: IncomingMessage,
    res: ServerResponse,
    next: Connect.NextFunction,
  ) => {
    const url = req.url ?? ''
    if (!url.startsWith('/api/analyse') || req.method !== 'POST') {
      return next()
    }

    await consumeRequestBody(req)
    await new Promise((resolve) => setTimeout(resolve, MOCK_DELAY_MS))

    const feedback =
      FEEDBACK_OPTIONS[Math.floor(Math.random() * FEEDBACK_OPTIONS.length)]

    res.setHeader('Content-Type', 'application/json')
    res.statusCode = 200
    res.end(JSON.stringify({ feedback }))
  }
}

function mockAnalysisApi() {
  return {
    name: 'mock-analysis-api',
    configureServer(server: ViteDevServer) {
      server.middlewares.use(mockAnalysisApiMiddleware())
    },
  }
}

export default defineConfig({
  plugins: [react(), tailwindcss(), mockAnalysisApi()],
})
