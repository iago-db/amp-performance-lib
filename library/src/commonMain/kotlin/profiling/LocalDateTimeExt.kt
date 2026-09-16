package com.example.pdfpoc.profiling

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

fun LocalDateTime.Companion.timestamp(): LocalDateTime {
    val timezone = TimeZone.currentSystemDefault()
    val now = Clock.System.now().toLocalDateTime(timezone)
    return now
}