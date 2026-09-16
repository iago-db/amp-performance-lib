package com.example.pdfpoc.profiling

import kotlinx.datetime.format
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
data class ProfilingReport(
    val name: String,
    val allocations: List<Profiler.Allocation>,
    val executions: List<ExecutionReport>,
    val children: List<ProfilingReport>
) {
    @OptIn(ExperimentalSerializationApi::class)
    @EncodeDefault
    var code: String? = null

    @OptIn(ExperimentalSerializationApi::class)
    @EncodeDefault
    val log = timeline().map { event ->
        val time = event.timestamp.format(Profiler.dateFormat)
        when (event) {
            is ProfilingEvent.AllocationEvent ->
                "$time [Allocation] - type: ${event.type}. bytes: ${event.bytes}. duration: ${event.duration}"
            is ProfilingEvent.ExecutionEvent ->
                "$time [Execution] - name: ${event.name}. bytes: ${event.bytes}. duration: ${event.duration}"
        }
    }

    fun timeline(): List<ProfilingEvent> {
        val events = mutableListOf<ProfilingEvent>()

        fun collect(node: ProfilingReport) {
            node.allocations.forEach {
                events += ProfilingEvent.AllocationEvent(it.timestamp, it.type, it.duration, it.bytes)
            }
            node.executions.forEach {
                events += ProfilingEvent.ExecutionEvent(it.timestamp, it.name, it.duration, it.bytes)
            }
            node.children.forEach(::collect)
        }
        collect(this)

        return events.sortedBy { it.timestamp }
    }
}