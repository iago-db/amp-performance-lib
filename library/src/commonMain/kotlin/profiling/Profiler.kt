package com.example.pdfpoc.profiling

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTimedValue

@Serializable
open class Profiler(internal val name: String) {

    private val allocations: MutableList<Allocation> = mutableListOf()
    private val executions: MutableList<Execution> = mutableListOf()
    private val children: MutableList<Profiler> = mutableListOf()

    private val mutex = Mutex()

    fun <T : Any> allocate(block: () -> T): T = runBlocking {
        val timestamp = LocalDateTime.timestamp()
        val bytesBefore = currentBytesUsage()
        val (value, duration) = measureTimedValue { block() }
        val bytesAfter = currentBytesUsage()

        val record = Allocation(
            value::class.simpleName ?: "Unknown",
            timestamp,
            duration.inWholeMilliseconds,
            bytesAfter - bytesBefore
        )

        mutex.withLock { allocations.add(record) }
        value
    }

    suspend fun <T> launch(name: String, block: suspend () -> T): T  {
        val execution = Execution(name)

        try {
            return execution.run(block)
        } finally {
            mutex.withLock { executions.add(execution) }
        }
    }

    fun <T> run(name: String, block: () -> T): T = runBlocking {
        launch(name, block)
    }

    fun <T : Profiler> addChild(block: () -> T): T {
        val child = block()
        children.add(child)
        return child
    }

    suspend fun report(): ProfilingReport = mutex.withLock {
        ProfilingReport(
            name = name,
            allocations = allocations.toList(),
            executions = executions.map { ExecutionReport(it.name, it.timestamp, it.duration, it.bytes) },
            children = children.map { it.report() }
        )
    }

    @Serializable
    data class Allocation(val type: String, val timestamp: LocalDateTime, val duration: Long, val bytes: Long)

    @Serializable
    class Execution(val name: String) {
        val timestamp = LocalDateTime.timestamp()
        var duration: Long = 0L
            private set
        var bytes: Long = 0L
            private set

        internal suspend fun <T> run(block: suspend () -> T): T = coroutineScope {
            val started = CompletableDeferred<Unit>()

            val samplingJob = launch(Dispatchers.Default) {
                val initialBytes = currentBytesUsage()
                started.complete(Unit)
                while (isActive) {
                    bytes = maxOf(bytes, currentBytesUsage() - initialBytes)
                    delay(10.milliseconds)
                }
            }
            started.await()

            try {
                val (value, elapsed) = measureTimedValue { block() }
                duration = elapsed.inWholeMilliseconds
                value
            } finally {
                samplingJob.cancelAndJoin()
            }
        }
    }

    companion object {
        val dateFormat: DateTimeFormat<LocalDateTime> = LocalDateTime.Format {
            hour()
            char(':')
            minute()
            char(':')
            second()
            char('.')
            secondFraction(3)
        }
    }
}