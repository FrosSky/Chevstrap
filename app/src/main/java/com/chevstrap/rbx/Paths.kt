package com.chevstrap.rbx

import java.io.File

object Paths {
    private var base = ""
    private var logsDir = ""
    private var customBackgroundDir = ""
    private var modificationsDir = ""

    @JvmStatic
    val localAppData: String
        get() = App.appContext?.filesDir?.absolutePath ?: ""

    @JvmStatic
    val logs: String
        get() {
            ensureInitialized()
            return logsDir
        }

    @JvmStatic
    val customBackground: String
        get() {
            ensureInitialized()
            return customBackgroundDir
        }

    @JvmStatic
    val modifications: String
        get() {
            ensureInitialized()
            return modificationsDir
        }

    val isInitialized: Boolean
        get() = base.isNotEmpty()

    @JvmStatic
    fun initialize(baseDirectory: String) {
        base = baseDirectory
        logsDir = File(base, "Logs").absolutePath
        customBackgroundDir = File(base, "CustomBackground").absolutePath
        modificationsDir = File(base, "Modifications").absolutePath
    }

    private fun ensureInitialized() {
        if (!isInitialized) {
            initialize(localAppData)
        }
    }
}