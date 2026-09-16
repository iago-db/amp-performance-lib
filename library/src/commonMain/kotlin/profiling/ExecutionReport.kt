package com.example.pdfpoc.profiling

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class ExecutionReport(val name: String, val timestamp: LocalDateTime, val duration: Long, val bytes: Long)
