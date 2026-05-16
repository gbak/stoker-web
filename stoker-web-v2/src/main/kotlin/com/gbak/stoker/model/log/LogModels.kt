package com.gbak.stoker.model.log

import com.gbak.stoker.model.devices.SDevice
import java.time.Instant

data class LogItem(
    val logName: String,
    val cookerName: String,
    val startTime: Instant = Instant.now(),
    val devices: List<SDevice> = emptyList(),
    val viewOnly: Boolean = false
)

data class LogNote(
    val noteDate: Instant = Instant.now(),
    val note: String
)

data class LogDir(
    val name: String,
    val path: String,
    val subdirectories: List<LogDir> = emptyList(),
    val files: List<String> = emptyList()
)

data class LogFileEntry(
    val fileName: String,
    val filePath: String,
    val relativePath: String,
    val cookerName: String,
    val logName: String,
    val startTime: Instant,
    val sizeBytes: Long
)
