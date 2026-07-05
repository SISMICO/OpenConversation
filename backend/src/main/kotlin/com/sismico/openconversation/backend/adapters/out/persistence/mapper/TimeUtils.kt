package com.sismico.openconversation.backend.adapters.out.persistence.mapper

import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

fun utcNow(): OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MICROS)

fun OffsetDateTime?.orUtcNow(): OffsetDateTime = this?.truncatedTo(ChronoUnit.MICROS) ?: utcNow()
