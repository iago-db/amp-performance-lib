package com.iago.app.profiler.profiling

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
sealed class ProfilingEvent {
    abstract val timestamp: LocalDateTime
    abstract val duration: Long
    abstract val bytes: Long

    @Serializable
    data class AllocationEvent(
        override val timestamp: LocalDateTime,
        val type: String,
        override val duration: Long,
        override val bytes: Long
    ) : ProfilingEvent()

    @Serializable
    data class ExecutionEvent(
        override val timestamp: LocalDateTime,
        val name: String,
        override val duration: Long,
        override val bytes: Long
    ) : ProfilingEvent()
}