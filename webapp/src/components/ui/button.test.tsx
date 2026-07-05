import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { Button } from './button'

describe('Button', () => {
  it('default variant renders with an indigo background and white text', () => {
    render(<Button>Default</Button>)
    const button = screen.getByRole('button', { name: 'Default' })

    expect(button).toHaveClass('bg-primary')
    expect(button).toHaveClass('text-primary-foreground')
  })

  it('secondary variant renders with a gray background and readable text', () => {
    render(<Button variant="secondary">Secondary</Button>)
    const button = screen.getByRole('button', { name: 'Secondary' })

    expect(button).toHaveClass('bg-secondary')
    expect(button).toHaveClass('text-secondary-foreground')
  })

  it('outline variant renders with a visible border and transparent background', () => {
    render(<Button variant="outline">Outline</Button>)
    const button = screen.getByRole('button', { name: 'Outline' })

    expect(button).toHaveClass('border-border')
    expect(button).toHaveClass('bg-transparent')
  })

  it('destructive variant renders with a muted rose tint and readable text', () => {
    render(<Button variant="destructive">Destructive</Button>)
    const button = screen.getByRole('button', { name: 'Destructive' })

    expect(button).toHaveClass('bg-destructive/15')
    expect(button).toHaveClass('text-destructive')
  })
})
