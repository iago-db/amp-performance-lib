package com.iago.app.profiler.profiling

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun LocalDateTime.Companion.timestamp(): LocalDateTime {
    val timezone = TimeZone.currentSystemDefault()
    val now = Clock.System.now().toLocalDateTime(timezone)
    return now
}