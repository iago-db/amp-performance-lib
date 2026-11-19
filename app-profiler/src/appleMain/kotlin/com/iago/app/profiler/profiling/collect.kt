package com.iago.app.profiler.profiling

import kotlin.native.runtime.GC
import kotlin.native.runtime.NativeRuntimeApi

@OptIn(NativeRuntimeApi::class)
actual fun collect() {
    GC.collect()
}