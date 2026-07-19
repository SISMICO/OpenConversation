import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { downloadBlob, getAudioExtension } from './utils'

describe('getAudioExtension', () => {
  it('returns .webm for audio/webm', () => {
    expect(getAudioExtension('audio/webm')).toBe('.webm')
  })

  it('returns .webm for audio/webm;codecs=opus', () => {
    expect(getAudioExtension('audio/webm;codecs=opus')).toBe('.webm')
  })

  it('returns .mp4 for audio/mp4', () => {
    expect(getAudioExtension('audio/mp4')).toBe('.mp4')
  })

  it('returns .bin for an unknown MIME type', () => {
    expect(getAudioExtension('audio/ogg')).toBe('.bin')
  })

  it('returns .bin for a null MIME type', () => {
    expect(getAudioExtension(null)).toBe('.bin')
  })

  it('handles MIME type casing and whitespace', () => {
    expect(getAudioExtension('  Audio/WEBM;codecs=opus  ')).toBe('.webm')
  })
})

describe('downloadBlob', () => {
  let createObjectURLSpy: ReturnType<typeof vi.spyOn>
  let revokeObjectURLSpy: ReturnType<typeof vi.spyOn>
  let clickSpy: ReturnType<typeof vi.spyOn>

  beforeEach(() => {
    createObjectURLSpy = vi
      .spyOn(URL, 'createObjectURL')
      .mockReturnValue('blob:mock-url')
    revokeObjectURLSpy = vi.spyOn(URL, 'revokeObjectURL').mockReturnValue(undefined)
    clickSpy = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {})
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('creates an anchor with the correct download filename and triggers download', () => {
    const blob = new Blob(['audio'], { type: 'audio/webm' })

    downloadBlob(blob, 'recording.webm')

    expect(createObjectURLSpy).toHaveBeenCalledWith(blob)
    expect(revokeObjectURLSpy).toHaveBeenCalledWith('blob:mock-url')
    expect(clickSpy).toHaveBeenCalled()
  })

  it('no-ops when blob is null', () => {
    downloadBlob(null, 'recording.webm')

    expect(createObjectURLSpy).not.toHaveBeenCalled()
    expect(clickSpy).not.toHaveBeenCalled()
    expect(revokeObjectURLSpy).not.toHaveBeenCalled()
  })

  it('creates and revokes a fresh object URL on each call', () => {
    const blob = new Blob(['audio'], { type: 'audio/webm' })

    createObjectURLSpy
      .mockReturnValueOnce('blob:first-url')
      .mockReturnValueOnce('blob:second-url')

    downloadBlob(blob, 'recording.webm')
    downloadBlob(blob, 'recording.webm')

    expect(createObjectURLSpy).toHaveBeenCalledTimes(2)
    expect(revokeObjectURLSpy).toHaveBeenCalledTimes(2)
    expect(revokeObjectURLSpy).toHaveBeenNthCalledWith(1, 'blob:first-url')
    expect(revokeObjectURLSpy).toHaveBeenNthCalledWith(2, 'blob:second-url')
  })
})
