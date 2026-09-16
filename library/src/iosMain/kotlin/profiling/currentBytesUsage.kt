package com.example.pdfpoc.profiling

// iosMain
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.value
import platform.darwin.*

@OptIn(ExperimentalForeignApi::class)
actual fun currentBytesUsage(): Long = memScoped {
    val info = alloc<task_vm_info_data_t>()
    val count = alloc<UIntVar>()
    count.value = (sizeOf<task_vm_info_data_t>() / sizeOf<IntVar>()).convert()

    val result = task_info(
        mach_task_self_,
        TASK_VM_INFO.toUInt(),
        info.ptr.reinterpret(),
        count.ptr
    )

    if (result == KERN_SUCCESS) info.phys_footprint.toLong() else 0L
}