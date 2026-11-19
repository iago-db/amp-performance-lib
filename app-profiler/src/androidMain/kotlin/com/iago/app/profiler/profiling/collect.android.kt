package com.iago.app.profiler.profiling

actual fun collect() {
    System.gc()
}