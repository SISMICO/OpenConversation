import { act } from '@testing-library/react'
import { ThemeProvider } from 'next-themes'
import type { ReactNode } from 'react'
import { describe, expect, it, vi } from 'vitest'

vi.mock('next-themes', () => ({
  ThemeProvider: vi.fn(({ children }: { children: ReactNode }) => children),
}))

vi.mock('./App.tsx', () => ({
  default: function App() {
    return <div data-testid="app">App</div>
  },
}))

describe('main', () => {
  it('renders ThemeProvider with attribute="class", defaultTheme="system", and enableSystem={true}', async () => {
    document.body.innerHTML = '<div id="root"></div>'

    await act(async () => {
      await import('./main')
    })

    const mockedThemeProvider = vi.mocked(ThemeProvider)
    expect(mockedThemeProvider).toHaveBeenCalled()

    const [props] = mockedThemeProvider.mock.calls[0]
    expect(props).toEqual(
      expect.objectContaining({
        attribute: 'class',
        defaultTheme: 'system',
        enableSystem: true,
      }),
    )
  })
})
