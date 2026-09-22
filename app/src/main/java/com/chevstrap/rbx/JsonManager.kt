package com.chevstrap.rbx

import chevstrap.extensions.FileTool
import com.chevstrap.rbx.utility.MD5Hash
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

abstract class JsonManager<T> {

    @JvmField
    protected var prop: T? = null

    @JvmField
    protected var originalProp: T? = null

    var isLoaded: Boolean = false
        protected set

    private var lastFileHash: String? = null

    protected val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        coerceInputValues = true
    }

    abstract val className: String

    abstract val fileLocation: File

    protected abstract val serializer: KSerializer<T>

    protected abstract fun createDefault(): T

    open fun save() {
        val file = fileLocation
        val value = prop ?: createDefault()

        try {
            val jsonData = json.encodeToString(
                serializer,
                value
            )

            FileTool.safeWrite(
                file,
                jsonData
            )

            lastFileHash = MD5Hash.fromFile(
                file.absolutePath
            )

            isLoaded = true
        } catch (e: IOException) {
            throw RuntimeException(
                "Failed to save $className: ${e.message}",
                e
            )
        } catch (e: Exception) {
            throw RuntimeException(
                "Failed to save $className: ${e.message}",
                e
            )
        }
    }

    open fun load(alertFailure: Boolean) {
        val file = fileLocation

        if (!file.exists()) {
            prop = createDefault()
            isLoaded = false
            lastFileHash = null
            return
        }

        try {
            val currentHash = MD5Hash.fromFile(
                file.absolutePath
            )

            if (!hasFileChanged() && isLoaded) {
                return
            }

            if (
                currentHash != null &&
                currentHash == lastFileHash
            ) {
                isLoaded = true
                return
            }

            val jsonData = FileTool.safeRead(file)

            prop = json.decodeFromString(
                serializer,
                jsonData
            )

            lastFileHash = currentHash
            isLoaded = true

        } catch (e: Exception) {
            handleLoadError(
                file,
                alertFailure,
                e
            )
        }
    }

    private fun handleLoadError(
        file: File,
        alertFailure: Boolean,
        exception: Exception
    ) {
        if (alertFailure) {
            App.logger.writeLine(
                "JsonManager::Load",
                "Failed to load $className: ${exception.message}"
            )
        }

        try {
            if (file.exists()) {
                val backupName = "${className}_backup.json"
                val backupFile = File(
                    file.parentFile,
                    backupName
                )

                FileTool.copy(
                    file,
                    backupFile,
                    true
                )

                App.logger.writeLine(
                    "JsonManager::Load",
                    "Backup created: ${backupFile.absolutePath}"
                )
            }
        } catch (copyException: Exception) {
            App.logger.writeLine(
                "JsonManager::Load",
                "Failed to create backup: ${copyException.message}"
            )
        }

        prop = createDefault()
        isLoaded = false
        lastFileHash = null

        save()
    }

    protected fun deepEquals(
        a: Any?,
        b: Any?
    ): Boolean {
        if (a === b) {
            return true
        }

        if (a == null || b == null) {
            return false
        }

        return when (a) {
            is Number if b is Number -> {
                a.toDouble() == b.toDouble()
            }

            is Map<*, *> if b is Map<*, *> -> {
                if (a.size != b.size) {
                    return false
                }

                a.all { (key, value) ->
                    b.containsKey(key) &&
                            deepEquals(
                                value,
                                b[key]
                            )
                }
            }

            is List<*> if b is List<*> -> {
                if (a.size != b.size) {
                    return false
                }

                a.indices.all { index ->
                    deepEquals(
                        a[index],
                        b[index]
                    )
                }
            }

            else -> {
                a == b
            }
        }
    }

    val currentFileHash: String?
        get() {
            val file = fileLocation

            return if (file.exists()) {
                MD5Hash.fromFile(
                    file.absolutePath
                )
            } else {
                null
            }
        }

    fun hasFileChanged(): Boolean {
        val currentHash = currentFileHash

        return currentHash != null &&
                currentHash != lastFileHash
    }
}