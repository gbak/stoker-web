package com.gbak.stoker.rest

import com.gbak.stoker.log.LogExistsException
import com.gbak.stoker.log.LogManager
import com.gbak.stoker.log.LogNotFoundException
import com.gbak.stoker.model.SDataPoint
import com.gbak.stoker.model.log.LogFileEntry
import com.gbak.stoker.model.log.LogItem
import com.gbak.stoker.model.log.LogNote
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/logs")
class LogController(
    private val logManager: LogManager
) {
    @GetMapping
    fun getLogList(): List<LogItem> = logManager.getLogList()

    @PostMapping("/start")
    fun startLog(@RequestBody request: StartLogRequest): ResponseEntity<Any> {
        return try {
            logManager.startLog(request.cookerName, request.logName)
            ResponseEntity.ok(mapOf("status" to "started", "logName" to request.logName))
        } catch (e: LogExistsException) {
            ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to e.message))
        }
    }

    @PostMapping("/stop")
    fun stopLog(@RequestBody request: StopLogRequest): ResponseEntity<Any> {
        return try {
            val fileName = logManager.stopLog(request.logName)
            ResponseEntity.ok(mapOf("status" to "stopped", "fileName" to fileName))
        } catch (e: LogNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to e.message))
        }
    }

    @GetMapping("/{logName}/data")
    fun getLogData(@PathVariable logName: String): Map<String, List<SDataPoint>> =
        logManager.getAllDataPoints(logName)

    @GetMapping("/{logName}/notes")
    fun getNotes(@PathVariable logName: String): List<LogNote> =
        logManager.getNotes(logName)

    @PostMapping("/{logName}/notes")
    fun addNote(@PathVariable logName: String, @RequestBody request: AddNoteRequest): ResponseEntity<Void> {
        logManager.addNoteToLog(request.note, request.logNames.ifEmpty { listOf(logName) })
        return ResponseEntity.ok().build()
    }

    @GetMapping("/files")
    fun listLogFiles(): List<LogFileEntry> = logManager.listLogFiles()

    @PostMapping("/load")
    fun loadLog(@RequestBody request: LoadLogRequest): ResponseEntity<Any> {
        return try {
            val logItem = logManager.loadLogFromFile(request.filePath, request.resume)
            ResponseEntity.ok(logItem)
        } catch (e: LogExistsException) {
            ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to e.message))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PostMapping("/close")
    fun closeViewLog(@RequestBody request: StopLogRequest): ResponseEntity<Void> {
        logManager.closeViewLog(request.logName)
        return ResponseEntity.ok().build()
    }
}

data class StartLogRequest(val cookerName: String, val logName: String)
data class StopLogRequest(val logName: String)
data class AddNoteRequest(val note: String, val logNames: List<String> = emptyList())
data class LoadLogRequest(val filePath: String, val resume: Boolean = false)
