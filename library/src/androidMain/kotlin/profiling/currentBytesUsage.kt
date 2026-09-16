package com.example.pdfpoc.profiling

import android.os.Debug

actual fun currentBytesUsage(): Long {
    val nativeHeap = Debug.getNativeHeapAllocatedSize()
    val runtime = Runtime.getRuntime()
    val jvmHeap = runtime.totalMemory() - runtime.freeMemory()
    return nativeHeap + jvmHeap
}