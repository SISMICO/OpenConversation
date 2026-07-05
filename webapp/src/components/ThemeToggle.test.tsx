import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { useTheme } from 'next-themes'
import type { ReactNode } from 'react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { ThemeToggle } from './ThemeToggle'

vi.mock('next-themes', () => ({
  ThemeProvider: ({ children }: { children: ReactNode }) => children,
  useTheme: vi.fn(),
}))

const mockedUseTheme = vi.mocked(useTheme)

function mockUseTheme(theme: string, setTheme = vi.fn()) {
  mockedUseTheme.mockReturnValue({
    theme,
    setTheme,
    resolvedTheme: theme,
    systemTheme: 'light',
    themes: ['light', 'dark', 'system'],
    forcedTheme: undefined,
    setForcedTheme: vi.fn(),
  } as unknown as ReturnType<typeof useTheme>)
}

afterEach(() => {
  vi.restoreAllMocks()
})

describe('ThemeToggle', () => {
  it('renders the three theme options: Light, Dark, and System', () => {
    mockUseTheme('system')
    render(<ThemeToggle />)

    expect(screen.getByRole('option', { name: 'Light' })).toBeInTheDocument()
    expect(screen.getByRole('option', { name: 'Dark' })).toBeInTheDocument()
    expect(screen.getByRole('option', { name: 'System' })).toBeInTheDocument()
  })

  it('has a non-transparent background and a visible text color', () => {
    mockUseTheme('system')
    render(<ThemeToggle />)

    const select = screen.getByLabelText('Theme')
    expect(select).toHaveClass('bg-background')
    expect(select).toHaveClass('text-foreground')
  })

  it('calls setTheme with the selected value when an option is chosen', async () => {
    const setTheme = vi.fn()
    mockUseTheme('system', setTheme)

    const user = userEvent.setup()
    render(<ThemeToggle />)

    const select = screen.getByLabelText('Theme')
    await user.selectOptions(select, 'dark')

    expect(setTheme).toHaveBeenCalledWith('dark')
  })
})
