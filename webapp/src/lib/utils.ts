import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function getAudioExtension(mimeType: string | null): string {
  if (!mimeType) {
    return '.bin'
  }

  const normalized = mimeType.toLowerCase().trim()

  if (normalized.startsWith('audio/webm')) {
    return '.webm'
  }

  if (normalized.startsWith('audio/mp4')) {
    return '.mp4'
  }

  return '.bin'
}

export function downloadBlob(blob: Blob | null, filename: string): void {
  if (!blob) {
    return
  }

  const url = URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = filename
  document.body.appendChild(anchor)
  anchor.click()
  anchor.remove()
  URL.revokeObjectURL(url)
}
