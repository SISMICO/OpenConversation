package com.sismico.openconversation.backend.application.ports.out.storage

import com.sismico.openconversation.backend.domain.AudioStorageReference

/**
 * Outbound port for persisting uploaded audio.
 *
 * The local filesystem adapter is the default implementation. Because this is an
 * interface in the application layer, an object-storage adapter (e.g., S3) can be
 * introduced later without changing domain or application code.
 */
interface AudioStoragePort {
    fun store(
        audio: ByteArray,
        originalFilename: String?,
    ): AudioStorageReference
}
