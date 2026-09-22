package com.chevstrap.rbx

import chevstrap.extensions.FileTool
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale

class Logger private constructor() {
    private val lock = Any()
    private var filestream: FileOutputStream? = null

    val history: MutableList<String> = Collections.synchronizedList(ArrayList())

    var initialized: Boolean = false
        private set

    var noWriteMode: Boolean = false
        private set

    var fileLocation: String? = null
        private set

    private var cleanupDone = false

    fun initializePersistent() {
        if (initialized || noWriteMode) return

        val directory = File(Paths.logs)
        if (!directory.exists() && !directory.mkdirs()) {
            noWriteMode = true
            return
        }

        if (!cleanupDone) {
            cleanupOldLogs(directory)
            cleanupDone = true
        }

        val timestamp = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US).format(Date())
        val file = File(directory, "Chevstrap_$timestamp.log")

        try {
            filestream = FileOutputStream(file, true)
            initialized = true
            fileLocation = file.absolutePath
            writeLine("Logger::Initialize", "Logger initialized at $fileLocation")

            if (history.isNotEmpty()) {
                val backlog = synchronized(history) { history.joinToString("\r\n") }
                writeToLog(backlog)
            }
        } catch (_: IOException) {
            noWriteMode = true
        }
    }

    private fun cleanupOldLogs(directory: File) {
        val cutoff = System.currentTimeMillis() - (3L * 24L * 60L * 60L * 1000L)

        try {
            val files = FileTool.listFiles(directory)
            for (file in files) {
                if (file.isFile && file.name.endsWith(".log")) {
                    if (file.lastModified() < cutoff) {
                        FileTool.deleteFile(file)
                    }
                }
            }
        } catch (e: IOException) {
            writeException("Logger::Cleanup", e)
        }
    }

    fun writeLine(identifier: String?, message: String?) {
        writeLineInternal("[$identifier] ${message ?: ""}")
    }

    fun writeException(identifier: String?, ex: Exception?) {
        val hResult = "0x" + Integer.toHexString((ex?.hashCode() ?: 0)).uppercase()
        writeLineInternal("[$identifier] ($hResult) ${ex ?: "null"}")
    }

    private fun writeLineInternal(message: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        val output = "$timestamp $message"

        println(output)
        history.add(output)
        writeToLog(output)
    }

    private fun writeToLog(message: String) {
        if (!initialized || filestream == null || noWriteMode) return

        synchronized(lock) {
            try {
                val bytes = "$message\r\n".toByteArray(StandardCharsets.UTF_8)
                filestream?.write(bytes)
                filestream?.flush()
            } catch (_: IOException) {
            }
        }
    }

    companion object {
        @Volatile
        private var instanceDelegate: Logger? = null

        @JvmStatic
        val instance: Logger
            get() {
                return instanceDelegate ?: synchronized(this) {
                    instanceDelegate ?: Logger().also { instanceDelegate = it }
                }
            }
    }
}