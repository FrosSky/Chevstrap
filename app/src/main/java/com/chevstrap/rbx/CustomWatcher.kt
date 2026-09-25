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
        get() {
            val history = activityWatcher?.history
                ?.filterNotNull()
                ?: return mutableListOf()

            val grouped = LinkedHashMap<Long, ActivityData>()

            for (data in history) {
                val universeId = data.universeId

                if (universeId == 0L) {
                    continue
                }

                val existing = grouped[universeId]

                if (existing == null) {
                    grouped[universeId] = data
                    continue
                }

                val joined = listOfNotNull(
                    existing.timeJoined,
                    data.timeJoined
                ).minOrNull()

                val left = listOfNotNull(
                    existing.timeLeft,
                    data.timeLeft
                ).maxOrNull()

                existing.timeJoined = joined
                existing.timeLeft = left
            }

            return grouped.values.toMutableList()
        }

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