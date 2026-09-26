package com.chevstrap.rbx

import com.chevstrap.rbx.integrations.ActivityWatcher
import com.chevstrap.rbx.integrations.RobloxDiscordRPC
import com.chevstrap.rbx.models.entities.ActivityData

class CustomWatcher private constructor() {

    @JvmField
    var activityData: ActivityData? = null

    var activityWatcher: ActivityWatcher? = null
        private set

    var robloxDiscordRPC: RobloxDiscordRPC? = null
        private set

    var isDisposed: Boolean = false
        private set

    private var running = false

    init {
        createWatcher()
    }

    private fun createWatcher() {
        try {
            val watcher = ActivityWatcher()
            activityWatcher = watcher
            robloxDiscordRPC = RobloxDiscordRPC(watcher)
        } catch (ex: Exception) {
            App.logger.writeException(LOG_IDENTIFIER, ex)
            activityWatcher = null
            robloxDiscordRPC = null
        }
    }

    @Synchronized
    fun dispose() {
        if (isDisposed) return

        isDisposed = true
        running = false

        App.logger.writeLine(LOG_IDENTIFIER, "Disposing CustomWatcher")

        val watcher = activityWatcher
        activityWatcher = null
        robloxDiscordRPC = null
        activityData = null

        try {
            if (watcher != null && !watcher.isStopMonitoring) {
                watcher.dispose()
            }
        } catch (ex: Exception) {
            App.logger.writeException(LOG_IDENTIFIER, ex)
        }

        synchronized(CustomWatcher::class.java) {
            if (instance === this) {
                instance = null
            }
        }
    }

    @Synchronized
    fun run() {
        if (isDisposed || running) return

        try {
            activityWatcher?.let {
                if (!it.isStopMonitoring) {
                    it.dispose()
                }
            }

            activityWatcher = null
            robloxDiscordRPC = null

            val watcher = ActivityWatcher()
            val rpc = RobloxDiscordRPC(watcher)

            activityWatcher = watcher
            robloxDiscordRPC = rpc

            running = true
            watcher.runWatcher()
        } catch (ex: Exception) {
            running = false
            activityWatcher = null
            robloxDiscordRPC = null
            App.logger.writeException(LOG_IDENTIFIER, ex)
        }
    }

     val allHistoryServer: MutableCollection<ActivityData>
        get() = activityWatcher?.history
            ?.filterNotNull()
            ?.toMutableList()
            ?: mutableListOf()

    companion object {
        private const val LOG_IDENTIFIER = "CustomWatcher"

        private var instance: CustomWatcher? = null

        @JvmStatic
        fun getInstance(): CustomWatcher {
            synchronized(CustomWatcher::class.java) {
                return instance ?: CustomWatcher().also {
                    instance = it
                }
            }
        }
    }
}
