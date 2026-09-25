package com.chevstrap.rbx.integrations

import chevstrap.extensions.FileTool
import com.chevstrap.rbx.App
import com.chevstrap.rbx.CustomWatcher
import com.chevstrap.rbx.appDirectories.RobloxClientData
import com.chevstrap.rbx.enums.ServerType
import com.chevstrap.rbx.models.entities.ActivityData
import com.chevstrap.rbx.ui.NotifyIconWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.util.Date
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.concurrent.Volatile

class ActivityWatcher {

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val watcherScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val history: MutableList<ActivityData?> = ArrayList()

    private var discordRPC: RobloxDiscordRPC? = null

    @Volatile
    var isStopMonitoring = false
        private set

    @Volatile
    var isInExperience = false

    @Volatile
    var isEnableRPC = false

    @JvmField
    var data: ActivityData? = null

    private fun start() {
        val tag = "ActivityWatcher::start"
        data = ActivityData("")

        val execPath = RobloxClientData().executablePath
        if (execPath.isNullOrEmpty()) {
            App.logger.writeLine(tag, "Executable path is null or empty")
            App.isLastLogFoundOrMaybeNot = true
            return
        }

        isEnableRPC = App.config.data.showGameActivity
        discordRPC = CustomWatcher.getInstance().robloxDiscordRPC

        val logLocation = File(execPath, "logs")
        if (!logLocation.exists() && !logLocation.mkdirs()) {
            App.logger.writeLine(tag, "Failed to create log directory: ${logLocation.absolutePath}")
            App.isLastLogFoundOrMaybeNot = true
            return
        }

        App.logger.writeLine(tag, "Watching logs from ${logLocation.absolutePath}")

        var randomAccessFile: RandomAccessFile? = null
        var currentLogPath: String? = null

        try {
            App.isLastLogFoundOrMaybeNot = true
            var latestLog = getLatestLog(logLocation)

            if (latestLog != null) {
                currentLogPath = latestLog.absolutePath
                randomAccessFile = RandomAccessFile(latestLog, "r")
                randomAccessFile.seek(randomAccessFile.length())
                App.logger.writeLine(
                    tag,
                    "Starting from EOF: ${latestLog.name} (${randomAccessFile.length()} bytes)"
                )
            } else {
                App.logger.writeLine(tag, "No existing Roblox log found. Waiting for a new log...")
            }

            while (!isStopMonitoring) {
                latestLog = getLatestLog(logLocation)

                if (latestLog == null) {
                    Thread.sleep(500)
                    continue
                }

                if (currentLogPath != latestLog.absolutePath) {
                    App.logger.writeLine(tag, "Detected new Roblox log: ${latestLog.name}")
                    try {
                        randomAccessFile?.close()
                    } catch (_: IOException) {
                    }

                    currentLogPath = latestLog.absolutePath
                    randomAccessFile = RandomAccessFile(latestLog, "r")
                    randomAccessFile.seek(0)
                    App.logger.writeLine(tag, "Now watching: ${latestLog.name}")
                    continue
                }

                if (randomAccessFile == null) {
                    randomAccessFile = RandomAccessFile(latestLog, "r")
                    randomAccessFile.seek(randomAccessFile.length())
                    currentLogPath = null
                }

                if (randomAccessFile.length() < randomAccessFile.filePointer) {
                    App.logger.writeLine(tag, "Log file was truncated. Resetting reader.")
                    randomAccessFile.seek(0)
                }

                var hasReadData = false

                while (!isStopMonitoring) {
                    val line = randomAccessFile.readLine() ?: break
                    hasReadData = true

                    val decodedLine = try {
                        String(
                            line.toByteArray(Charsets.ISO_8859_1),
                            Charsets.UTF_8
                        )
                    } catch (_: Exception) {
                        line
                    }

                    handleLogEntry(decodedLine)
                }

                if (!hasReadData) Thread.sleep(500)
            }
        } catch (e: InterruptedException) {
            if (!isStopMonitoring) {
                App.logger.writeLine(tag, "Activity watcher interrupted unexpectedly.")
                App.logger.writeException(tag, e)
            } else {
                App.logger.writeLine(tag, "Activity watcher stopped.")
            }
            Thread.currentThread().interrupt()
        } catch (e: IOException) {
            App.logger.writeLine(tag, "Failed to run activity watcher!")
            App.logger.writeException(tag, e)
        } catch (e: Exception) {
            App.logger.writeLine(tag, "Unexpected error in activity watcher!")
            App.logger.writeException(tag, e)
        } finally {
            App.isLastLogFoundOrMaybeNot = true
            closeRandomAccessFile(randomAccessFile)
        }
    }

    private fun getLatestLog(logLocation: File): File? {
        return try {
            FileTool.listFiles(logLocation)
                .filter { it.isFile }
                .maxWithOrNull(
                    compareBy<File> { it.lastModified() }
                        .thenBy { it.length() }
                )
        } catch (e: Exception) {
            App.logger.writeException("ActivityWatcher::getLatestLog", e)
            null
        }
    }

    private fun closeRandomAccessFile(file: RandomAccessFile?) {
        try {
            file?.close()
        } catch (_: IOException) {
        }
    }

    private fun updateServerType(activityData: ActivityData) {
        val rootPlaceId = activityData.rootPlaceId
        val placeId = activityData.placeId

        activityData.serverType = when {
            rootPlaceId == 0L || placeId == 0L || rootPlaceId == placeId ->
                ServerType.PUBLIC

            else -> ServerType.RESERVED
        }

        App.logger.writeLine(
            "ActivityWatcher::updateServerType",
            "Server type updated: rootPlaceId=$rootPlaceId, " +
                    "placeId=$placeId, serverType=${activityData.serverType}"
        )
    }

    private fun handleLogEntry(line: String) {
        val tag = "ActivityWatcher::handleLogEntry"
        val joinServer = "[FLog::Output] ! Joining game"
        val leaveServer = "[FLog::Network] Time to disconnect replication data"
        val connection = "Info [DFLog::NetworkClient] Connection accepted from"
        val leavingRoblox = "[FLog::SingleSurfaceApp] setStage: (stage:None)"
        val universe = "[FLog::GameJoinLoadTime] Report game_join_loadtime:"
        val bsRPC = "[BloxstrapRPC]"

        if (
            !line.contains(joinServer) &&
            !line.contains(leaveServer) &&
            !line.contains(connection) &&
            !line.contains(bsRPC) &&
            !line.contains(leavingRoblox) &&
            !line.contains(universe)
        ) return

        val activityData = data ?: return

        try {
            if (!isInExperience && line.contains(leavingRoblox)) {
                dispose()
                return
            }

            if (
                isInExperience &&
                activityData.placeId != 0L &&
                line.contains(leaveServer)
            ) {
                activityData.timeLeft = Date()
                updateServerType(activityData)
                history.add(activityData)

                App.logger.writeLine(
                    tag,
                    "Added activity to history: serverType=${activityData.serverType}, " +
                            "rootPlaceId=${activityData.rootPlaceId}, " +
                            "placeId=${activityData.placeId}"
                )

                isInExperience = false
                data = ActivityData("")

                App.appContext?.let {
                    NotifyIconWrapper.hideConnectionNotification(it)

                    if (isEnableRPC) {
                        NotifyIconWrapper.hideDiscordRpcVisibilityNotification(it)
                    }
                }

                if (isEnableRPC) discordRPC?.setCurrentGame()
                return
            }

            if (
                !isInExperience &&
                activityData.placeId == 0L &&
                line.contains(joinServer)
            ) {
                try {
                    val match = Regex(
                        """Joining game '([^']+)' place (\d+) at (.+)"""
                    ).find(line)

                    if (match != null) {
                        val jobId = match.groupValues[1].trim()
                        val placeId = match.groupValues[2].trim().toLongOrNull()
                        val serverIP = match.groupValues[3].trim()

                        if (jobId.isNotEmpty() && placeId != null) {
                            App.logger.writeLine(
                                tag,
                                "Joining game: placeId=$placeId, jobId=$jobId, serverIP=$serverIP"
                            )

                            isInExperience = true
                            activityData.timeJoined = Date()
                            activityData.placeId = placeId
                            activityData.jobId = jobId
                            updateServerType(activityData)
                        } else {
                            App.logger.writeLine(
                                tag,
                                "Failed to parse join experience info or game info: $line"
                            )
                        }
                    } else {
                        App.logger.writeLine(
                            tag,
                            "Failed to parse join experience info or game info: $line"
                        )
                    }
                } catch (e: Exception) {
                    App.logger.writeLine(
                        tag,
                        "Failed to parse join experience info or game info"
                    )
                    App.logger.writeException(tag, e)
                }

                return
            }

            if (
                isInExperience &&
                activityData.placeId != 0L &&
                line.contains(connection)
            ) {
                try {
                    val match = Regex(
                        """Connection accepted from\s+(.+)$"""
                    ).find(line)

                    if (match != null) {
                        val location = match.groupValues[1]
                            .trim()
                            .substringBefore("|")
                            .trim()

                        if (location.isNotEmpty()) {
                            App.logger.writeLine(tag, "Connection string: $line")
                            activityData.machineAddress = location

                            if (
                                activityData.machineAddress.isNullOrEmpty() ||
                                activityData.universeId.toULong() == 0UL
                            ) {
                                App.logger.writeLine(
                                    tag,
                                    "Failed to parse connection string: $line"
                                )
                                App.logger.writeLine(
                                    tag,
                                    "Machine address: ${activityData.machineAddress}"
                                )
                                App.logger.writeLine(
                                    tag,
                                    "Universe ID: ${activityData.universeId}"
                                )
                            } else {
                                App.appContext?.let {
                                    NotifyIconWrapper.showConnectionNotification(
                                        it,
                                        activityData,
                                        location,
                                        activityData.universeId.toString()
                                    )

                                    if (isEnableRPC) {
                                        NotifyIconWrapper.showDiscordRpcVisibilityNotification(
                                            it,
                                            discordRPC?.isVisibleState() != null
                                        )
                                    }
                                }
                            }
                        } else {
                            App.logger.writeLine(
                                tag,
                                "Unexpected connection format: $line"
                            )
                        }
                    } else {
                        App.logger.writeLine(
                            tag,
                            "Unexpected connection format: $line"
                        )
                    }
                } catch (e: Exception) {
                    App.logger.writeLine(
                        tag,
                        "Failed to parse connection string"
                    )
                    App.logger.writeException(tag, e)
                }

                return
            }

            if (
                isInExperience &&
                activityData.placeId != 0L &&
                line.contains(universe)
            ) {
                try {
                    val userIndex = line.indexOf("userid:")

                    if (userIndex != -1) {
                        val userStart = userIndex + "userid:".length
                        val userEnd = line.indexOf(",", userStart)
                            .takeIf { it != -1 }
                            ?: line.length

                        line.substring(userStart, userEnd)
                            .trim()
                            .toLongOrNull()
                            ?.let { activityData.userId = it }
                    }

                    val universeIndex = line.indexOf("universeid:")

                    if (universeIndex != -1) {
                        val universeStart = universeIndex + "universeid:".length
                        val universeEnd = line.indexOf(",", universeStart)
                            .takeIf { it != -1 }
                            ?: line.length

                        line.substring(universeStart, universeEnd)
                            .trim()
                            .toLongOrNull()
                            ?.let { universeId ->
                                activityData.setUniverseId(universeId) { success ->
                                    if (success) {
                                        updateServerType(activityData)
                                        if (isEnableRPC) {
                                            discordRPC?.setCurrentGame()
                                        }
                                    }
                                }
                            }
                    }
                } catch (e: Exception) {
                    App.logger.writeLine(
                        tag,
                        "Failed to parse user/universe id info"
                    )
                    App.logger.writeException(tag, e)
                }

                return
            }

            if (line.contains(bsRPC) && isInExperience) {
                if (!App.config.data.discordAllowCustomPresence) return

                val jsonStart = line.indexOf("{", line.indexOf(bsRPC))

                if (jsonStart == -1) {
                    App.logger.writeLine(
                        tag,
                        "Failed to assert format for RPC message entry"
                    )
                    App.logger.writeLine(tag, line)
                    return
                }

                val messagePlain = line.substring(jsonStart).trim()
                App.logger.writeLine(tag, "Received message: '$messagePlain'")

                try {
                    if (isEnableRPC) {
                        discordRPC?.processRPCMessage(JSONObject(messagePlain))
                    }
                } catch (e: Exception) {
                    App.logger.writeLine(
                        tag,
                        "Failed to parse message! (JSON deserialization threw an exception)"
                    )
                    App.logger.writeException(tag, e)
                }
            }
        } catch (e: Exception) {
            App.logger.writeLine(tag, "Failed to handle log entry")
            App.logger.writeException(tag, e)
        }
    }

    internal fun isInExperienceState(): Boolean = isInExperience

    internal fun getActivityData(): ActivityData? = data

    fun dispose() {
        val tag = "ActivityWatcher::dispose"
        if (isStopMonitoring) return

        isStopMonitoring = true
        App.logger.writeLine(tag, "Disposing activity watcher session")

        try {
            if (isEnableRPC) discordRPC?.dispose()
        } catch (e: Exception) {
            App.logger.writeLine(tag, "Failed to dispose Discord RPC.")
            App.logger.writeException(tag, e)
        }

        App.appContext?.let {
            NotifyIconWrapper.hideConnectionNotification(it)

            if (isEnableRPC) {
                NotifyIconWrapper.hideDiscordRpcVisibilityNotification(it)
            }
        }

        watcherScope.cancel()
        if (!executor.isShutdown) executor.shutdownNow()
    }

    fun runWatcher() {
        val tag = "ActivityWatcher::runWatcher"

        if (isStopMonitoring) {
            App.logger.writeLine(
                tag,
                "Cannot start activity watcher because it has been disposed."
            )
            return
        }

        if (executor.isShutdown) {
            App.logger.writeLine(
                tag,
                "Cannot start activity watcher because executor is shut down."
            )
            return
        }

        App.logger.writeLine(tag, "Starting activity watcher session")

        try {
            executor.submit { start() }
        } catch (e: Exception) {
            App.logger.writeLine(
                tag,
                "Failed to submit activity watcher task."
            )
            App.logger.writeException(tag, e)
        }
    }
}