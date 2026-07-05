package com.sismico.openconversation.backend.adapters.out.storage

import com.sismico.openconversation.backend.application.ports.out.storage.AudioStoragePort
import com.sismico.openconversation.backend.config.LocalAudioStorageProperties
import com.sismico.openconversation.backend.domain.AudioStorageReference
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.UUID

class LocalAudioStorageAdapter(
    private val properties: LocalAudioStorageProperties,
) : AudioStoragePort {
    override fun store(
        audio: ByteArray,
        originalFilename: String?,
    ): AudioStorageReference {
        val extension = originalFilename?.extension() ?: DEFAULT_EXTENSION
        val filename = "${UUID.randomUUID()}.$extension"
        val basePath = Path.of(properties.basePath)
        val targetPath = basePath.resolve(filename)

        Files.createDirectories(basePath)
        Files.write(
            targetPath,
            audio,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
        )

        return AudioStorageReference("local://${targetPath.toAbsolutePath()}")
    }

    private fun String.extension(): String {
        val dot = lastIndexOf('.')
        return if (dot != -1 && dot < length - 1) {
            substring(dot + 1).lowercase().takeIf { it.isNotBlank() }
        } else {
            null
        } ?: DEFAULT_EXTENSION
    }

    companion object {
        private const val DEFAULT_EXTENSION = "webm"
    }
}
