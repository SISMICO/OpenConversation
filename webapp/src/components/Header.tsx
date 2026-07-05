import { cn } from '#lib/utils'
import { ThemeToggle } from './ThemeToggle'

interface HeaderProps {
  className?: string
}

export function Header({ className }: HeaderProps) {
  return (
    <header
      className={cn(
        'flex items-center justify-between border-b border-border px-6 py-4',
        className,
      )}
    >
      <span className="text-lg font-semibold text-foreground">
        OpenConversation
      </span>
      <ThemeToggle />
    </header>
  )
}
