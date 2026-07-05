import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { Header } from './Header'

describe('Header', () => {
  it('renders the app name "OpenConversation"', () => {
    render(<Header />)
    expect(screen.getByText('OpenConversation')).toBeInTheDocument()
  })

  it('renders the ThemeToggle inside the header', () => {
    render(<Header />)
    const header = screen.getByRole('banner')
    const toggle = screen.getByLabelText('Theme')

    expect(header).toBeInTheDocument()
    expect(toggle).toBeInTheDocument()
    expect(header).toContainElement(toggle)
  })
})
