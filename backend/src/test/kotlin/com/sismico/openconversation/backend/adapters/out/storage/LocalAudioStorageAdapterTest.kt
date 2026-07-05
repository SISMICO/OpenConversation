package com.sismico.openconversation.backend.adapters.out.storage

import com.sismico.openconversation.backend.config.LocalAudioStorageProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class LocalAudioStorageAdapterTest {
    @field:TempDir
    lateinit var tempDir: Path

    @Test
    fun `store creates file under base path`() {
        val adapter = createAdapter(tempDir)
        val audio = byteArrayOf(1, 2, 3, 4, 5)

        val reference = adapter.store(audio, "recording.webm")

        val storedPath = Path.of(reference.ref.removePrefix("local://"))
        assertTrue(Files.exists(storedPath), "Stored file should exist at $storedPath")
        assertEquals(audio.size.toLong(), Files.size(storedPath))
        assertEquals(audio.toList(), Files.readAllBytes(storedPath).toList())
        assertTrue(reference.ref.startsWith("local://"))
        assertTrue(storedPath.isAbsolute)
    }

    @Test
    fun `store preserves original extension when provided`() {
        val adapter = createAdapter(tempDir)
        val audio = byteArrayOf(1, 2, 3)

        val reference = adapter.store(audio, "voice.MP3")

        assertTrue(reference.ref.endsWith(".mp3"), "Expected .mp3 extension but was $reference")
    }

    @Test
    fun `store uses default extension when original filename is null`() {
        val adapter = createAdapter(tempDir)
        val audio = byteArrayOf(1, 2, 3)

        val reference = adapter.store(audio, null)

        assertTrue(reference.ref.endsWith(".webm"), "Expected .webm extension but was $reference")
    }

    @Test
    fun `store uses default extension when original filename has no extension`() {
        val adapter = createAdapter(tempDir)
        val audio = byteArrayOf(1, 2, 3)

        val reference = adapter.store(audio, "recording")

        assertTrue(reference.ref.endsWith(".webm"), "Expected .webm extension but was $reference")
    }

    @Test
    fun `store creates missing parent directories`() {
        val nestedBase = tempDir.resolve("nested/deep/storage")
        val adapter = createAdapter(nestedBase)
        val audio = byteArrayOf(1, 2, 3)

        val reference = adapter.store(audio, "clip.ogg")

        assertTrue(Files.exists(nestedBase), "Parent directories should be created")
        assertTrue(reference.ref.contains("nested/deep/storage"))
        assertTrue(reference.ref.endsWith(".ogg"))
    }

    private fun createAdapter(basePath: Path): LocalAudioStorageAdapter =
        LocalAudioStorageAdapter(LocalAudioStorageProperties(basePath.toAbsolutePath().toString()))
}
