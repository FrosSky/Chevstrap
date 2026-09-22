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

    private val executor: ExecutorService =
        Executors.newSingleThreadExecutor()

    val history: MutableList<ActivityData?> =
        ArrayList()

    private val watcherScope =
        CoroutineScope(
            SupervisorJob() + Dispatchers.IO
        )

    private var discordRPC: RobloxDiscordRPC? = null

    @Volatile
    var isStopMonitoring: Boolean = false
        private set

    @Volatile
    var isInExperience = false

    @Volatile
    var isEnableRPC = false

    @JvmField
    var data: ActivityData? = null

    private fun start() {
        val logIdentifier =
            "ActivityWatcher::start"

        data =
            ActivityData("")

        val robloxData =
            RobloxClientData()

        val execPath =
            robloxData.executablePath

        if (execPath.isNullOrEmpty()) {
            App.logger.writeLine(
                logIdentifier,
                "Executable path is null or empty"
            )
            App.isLastLogFoundOrMaybeNot =
                true

            return
        }

        isEnableRPC = App.config.data.showGameActivity
        discordRPC = CustomWatcher.getInstance().robloxDiscordRPC

        val logLocation =
            File(
                execPath,
                "logs"
            )

        if (
            !logLocation.exists() &&
            !logLocation.mkdirs()
        ) {
            App.logger.writeLine(
                logIdentifier,
                "Failed to create log directory: " +
                        logLocation.absolutePath
            )

            App.isLastLogFoundOrMaybeNot =
                true

            return
        }

        App.logger.writeLine(
            logIdentifier,
            "Watching logs from ${logLocation.absolutePath}"
        )

        var randomAccessFile: RandomAccessFile? =
            null

        var currentLogPath: String? =
            null

        try {
            App.isLastLogFoundOrMaybeNot =
                true

            var latestLog =
                getLatestLog(logLocation)

            if (latestLog != null) {
                currentLogPath =
                    latestLog.absolutePath

                randomAccessFile =
                    RandomAccessFile(
                        latestLog,
                        "r"
                    )

                val startPosition =
                    randomAccessFile.length()

                randomAccessFile.seek(
                    startPosition
                )

                App.logger.writeLine(
                    logIdentifier,
                    "Starting from EOF: " +
                            "${latestLog.name} " +
                            "($startPosition bytes)"
                )
            } else {
                App.logger.writeLine(
                    logIdentifier,
                    "No existing Roblox log found. " +
                            "Waiting for a new log..."
                )
            }

            while (!isStopMonitoring) {
                latestLog =
                    getLatestLog(logLocation)

                if (latestLog == null) {
                    Thread.sleep(500)
                    continue
                }

                val latestLogPath =
                    latestLog.absolutePath

                if (
                    currentLogPath !=
                    latestLogPath
                ) {
                    App.logger.writeLine(
                        logIdentifier,
                        "Detected new Roblox log: " +
                                latestLog.name
                    )

                    closeRandomAccessFile(
                        randomAccessFile
                    )

                    currentLogPath =
                        latestLogPath

                    randomAccessFile =
                        RandomAccessFile(
                            latestLog,
                            "r"
                        )

                    randomAccessFile.seek(0L)

                    App.logger.writeLine(
                        logIdentifier,
                        "Now watching: " +
                                latestLog.name
                    )

                    continue
                }

                if (randomAccessFile == null) {
                    randomAccessFile =
                        RandomAccessFile(
                            latestLog,
                            "r"
                        )

                    randomAccessFile.seek(
                        randomAccessFile.length()
                    )

                    currentLogPath =
                        null
                }

                val fileLength =
                    randomAccessFile.length()

                val currentPosition =
                    randomAccessFile.filePointer

                if (
                    fileLength <
                    currentPosition
                ) {
                    App.logger.writeLine(
                        logIdentifier,
                        "Log file was truncated. " +
                                "Resetting reader."
                    )

                    randomAccessFile.seek(0L)
                }

                var hasReadData =
                    false

                while (!isStopMonitoring) {
                    val line =
                        randomAccessFile.readLine()
                            ?: break

                    hasReadData =
                        true

                    val decodedLine =
                        try {
                            String(
                                line.toByteArray(
                                    Charsets.ISO_8859_1
                                ),
                                Charsets.UTF_8
                            )
                        } catch (_: Exception) {
                            line
                        }

                    handleLogEntry(
                        decodedLine
                    )
                }

                if (!hasReadData) {
                    Thread.sleep(500)
                }
            }
        } catch (
            e: InterruptedException
        ) {
            if (!isStopMonitoring) {
                App.logger.writeLine(
                    logIdentifier,
                    "Activity watcher interrupted unexpectedly."
                )

                App.logger.writeException(
                    logIdentifier,
                    e
                )
            } else {
                App.logger.writeLine(
                    logIdentifier,
                    "Activity watcher stopped."
                )
            }

            Thread.currentThread().interrupt()
        } catch (
            e: IOException
        ) {
            App.logger.writeLine(
                logIdentifier,
                "Failed to run activity watcher!"
            )

            App.logger.writeException(
                logIdentifier,
                e
            )
        } catch (
            e: Exception
        ) {
            App.logger.writeLine(
                logIdentifier,
                "Unexpected error in activity watcher!"
            )

            App.logger.writeException(
                logIdentifier,
                e
            )
        } finally {
            App.isLastLogFoundOrMaybeNot =
                true

            closeRandomAccessFile(
                randomAccessFile
            )
        }
    }

    private fun getLatestLog(
        logLocation: File
    ): File? {
        return try {
            FileTool.listFiles(
                logLocation
            )
                .filter {
                    it.isFile
                }
                .maxWithOrNull(
                    compareBy<File> {
                        it.lastModified()
                    }.thenBy {
                        it.length()
                    }
                )
        } catch (
            e: Exception
        ) {
            App.logger.writeException(
                "ActivityWatcher::getLatestLog",
                e
            )

            null
        }
    }

    private fun closeRandomAccessFile(
        file: RandomAccessFile?
    ) {
        try {
            file?.close()
        } catch (_: IOException) {
        }
    }

    private fun updateServerType(
        activityData: ActivityData
    ) {
        val rootPlaceId =
            activityData.rootPlaceId

        val placeId =
            activityData.placeId

        activityData.serverType =
            when {
                rootPlaceId == 0L ||
                        placeId == 0L -> {
                    ServerType.PUBLIC
                }

                rootPlaceId == placeId -> {
                    ServerType.PUBLIC
                }

                else -> {
                    ServerType.RESERVED
                }
            }

        App.logger.writeLine(
            "ActivityWatcher::updateServerType",
            "Server type updated: " +
                    "rootPlaceId=$rootPlaceId, " +
                    "placeId=$placeId, " +
                    "serverType=${activityData.serverType}"
        )
    }

    private fun handleLogEntry(
        line: String
    ) {
        val logIdentifier =
            "ActivityWatcher::handleLogEntry"

        val joinAServerStr =
            "[FLog::Output] ! Joining game"

        val leaveAServerStr =
            "[FLog::Network] Time to disconnect replication data"

        val connectionStr =
            "Info [DFLog::NetworkClient] Connection accepted from"

        val leavingRobloxStr =
            "[FLog::SingleSurfaceApp] setStage: (stage:None)"

        val universeStr =
            "[FLog::GameJoinLoadTime] Report game_join_loadtime:"

        val bloxstrapRPC =
            "[BloxstrapRPC]"

        try {
            if (
                !line.contains(joinAServerStr) &&
                !line.contains(leaveAServerStr) &&
                !line.contains(connectionStr) &&
                !line.contains(bloxstrapRPC) &&
                !line.contains(leavingRobloxStr) &&
                !line.contains(universeStr)
            ) {
                return
            }

            val activityData =
                data
                    ?: return

            if (
                !isInExperience &&
                line.contains(
                    leavingRobloxStr
                )
            ) {
                dispose()
                return
            }

            if (
                isInExperience &&
                activityData.placeId != 0L &&
                line.contains(
                    leaveAServerStr
                )
            ) {
                activityData.timeLeft =
                    Date()

                updateServerType(
                    activityData
                )

                history.add(
                    activityData
                )

                App.logger.writeLine(
                    logIdentifier,
                    "Added activity to history: " +
                            "serverType=${activityData.serverType}, " +
                            "rootPlaceId=${activityData.rootPlaceId}, " +
                            "placeId=${activityData.placeId}"
                )

                isInExperience =
                    false

                data =
                    ActivityData("")

                App.appContext?.let {
                    NotifyIconWrapper
                        .hideConnectionNotification(
                            it
                        )
                }

                if (isEnableRPC) {
                    App.appContext?.let {
                        NotifyIconWrapper
                            .hideDiscordRpcVisibilityNotification(
                                it
                            )
                    }
                }

                if (isEnableRPC) {
                    discordRPC?.setCurrentGame()
                }

                return
            }

            if (
                !isInExperience &&
                activityData.placeId == 0L &&
                line.contains(
                    joinAServerStr
                )
            ) {
                try {
                    val pattern =
                        Regex(
                            """Joining game '([^']+)' place (\d+) at (.+)"""
                        )

                    val match =
                        pattern.find(line)

                    if (match != null) {
                        val jobId =
                            match.groupValues[1]
                                .trim()

                        val placeIdStr =
                            match.groupValues[2]
                                .trim()

                        val serverIP =
                            match.groupValues[3]
                                .trim()

                        val placeId =
                            placeIdStr.toLongOrNull()

                        if (
                            jobId.isNotEmpty() &&
                            placeId != null
                        ) {
                            App.logger.writeLine(
                                logIdentifier,
                                "Joining game: " +
                                        "placeId=$placeId, " +
                                        "jobId=$jobId, " +
                                        "serverIP=$serverIP"
                            )

                            isInExperience =
                                true

                            activityData.timeJoined =
                                Date()

                            activityData.placeId =
                                placeId

                            activityData.jobId =
                                jobId

                            updateServerType(
                                activityData
                            )
                        } else {
                            App.logger.writeLine(
                                logIdentifier,
                                "Failed to parse join experience " +
                                        "info or game info: $line"
                            )
                        }
                    } else {
                        App.logger.writeLine(
                            logIdentifier,
                            "Failed to parse join experience " +
                                    "info or game info: $line"
                        )
                    }
                } catch (
                    e: Exception
                ) {
                    App.logger.writeLine(
                        logIdentifier,
                        "Failed to parse join experience " +
                                "info or game info"
                    )

                    App.logger.writeException(
                        logIdentifier,
                        e
                    )
                }

                return
            }

            if (
                isInExperience &&
                activityData.placeId != 0L &&
                line.contains(
                    connectionStr
                )
            ) {
                try {
                    val pattern =
                        Regex(
                            """Connection accepted from\s+(.+)$"""
                        )

                    val match =
                        pattern.find(line)

                    if (match != null) {
                        val rawAddress =
                            match.groupValues[1]
                                .trim()

                        val location =
                            rawAddress
                                .substringBefore("|")
                                .trim()

                        if (
                            location.isNotEmpty()
                        ) {
                            App.logger.writeLine(
                                logIdentifier,
                                "Connection string: $line"
                            )

                            activityData.machineAddress =
                                location

                            val address =
                                activityData.machineAddress

                            if (
                                !address.isNullOrEmpty() &&
                                activityData.universeId.toULong() != 0UL
                            ) {
                                App.appContext?.let {
                                    NotifyIconWrapper
                                        .showConnectionNotification(
                                            it,
                                            data!!,
                                            address,
                                            activityData
                                                .universeId
                                                .toString()
                                        )
                                }

                                if (isEnableRPC) {
                                    App.appContext?.let {
                                        NotifyIconWrapper
                                            .showDiscordRpcVisibilityNotification(
                                                it,
                                                discordRPC?.isVisibleState() != null
                                            )
                                    }
                                }
                            } else {
                                App.logger.writeLine(
                                    logIdentifier,
                                    "Failed to parse connection string: $line"
                                )

                                App.logger.writeLine(
                                    logIdentifier,
                                    "Machine address: " +
                                            activityData.machineAddress
                                )

                                App.logger.writeLine(
                                    logIdentifier,
                                    "Universe ID: " +
                                            activityData.universeId
                                )
                            }
                        } else {
                            App.logger.writeLine(
                                logIdentifier,
                                "Unexpected connection format: $line"
                            )
                        }
                    } else {
                        App.logger.writeLine(
                            logIdentifier,
                            "Unexpected connection format: $line"
                        )
                    }
                } catch (
                    e: Exception
                ) {
                    App.logger.writeLine(
                        logIdentifier,
                        "Failed to parse connection string"
                    )

                    App.logger.writeException(
                        logIdentifier,
                        e
                    )
                }

                return
            }

            if (
                isInExperience &&
                activityData.placeId != 0L &&
                line.contains(
                    universeStr
                )
            ) {
                try {
                    val userIndex =
                        line.indexOf(
                            "userid:"
                        )

                    if (userIndex != -1) {
                        var userEnd =
                            line.indexOf(
                                ",",
                                userIndex
                            )

                        if (userEnd == -1) {
                            userEnd =
                                line.length
                        }

                        val userIdStr =
                            line.substring(
                                userIndex +
                                        "userid:".length,
                                userEnd
                            ).trim()

                        userIdStr
                            .toLongOrNull()
                            ?.let {
                                activityData.userId =
                                    it
                            }
                    }

                    val universeIndex =
                        line.indexOf(
                            "universeid:"
                        )

                    if (
                        universeIndex != -1
                    ) {
                        var universeEnd =
                            line.indexOf(
                                ",",
                                universeIndex
                            )

                        if (universeEnd == -1) {
                            universeEnd =
                                line.length
                        }

                        val universeIdStr =
                            line.substring(
                                universeIndex +
                                        "universeid:".length,
                                universeEnd
                            ).trim()

                        val universeId =
                            universeIdStr
                                .toLongOrNull()

                        if (
                            universeId != null
                        ) {
                            activityData.setUniverseId(
                                universeId
                            ) { success ->
                                if (success) {
                                    updateServerType(
                                        activityData
                                    )

                                    if (isEnableRPC) {
                                        discordRPC?.setCurrentGame()
                                    }
                                }
                            }
                        }
                    }
                } catch (
                    e: Exception
                ) {
                    App.logger.writeLine(
                        logIdentifier,
                        "Failed to parse user/universe id info"
                    )

                    App.logger.writeException(
                        logIdentifier,
                        e
                    )
                }

                return
            }

            if (
                line.contains(
                    bloxstrapRPC
                ) &&
                isInExperience
            ) {
                if (
                    !App.config.data.discordAllowCustomPresence
                ) {
                    return
                }

                val matchStart =
                    line.indexOf(
                        bloxstrapRPC
                    )

                if (matchStart == -1) {
                    App.logger.writeLine(
                        logIdentifier,
                        "Failed to assert format for RPC message entry"
                    )

                    App.logger.writeLine(
                        logIdentifier,
                        line
                    )

                    return
                }

                val jsonStart =
                    line.indexOf(
                        "{",
                        matchStart
                    )

                if (jsonStart == -1) {
                    App.logger.writeLine(
                        logIdentifier,
                        "Failed to assert format for RPC message entry"
                    )

                    App.logger.writeLine(
                        logIdentifier,
                        line
                    )

                    return
                }

                val messagePlain =
                    line.substring(
                        jsonStart
                    ).trim()

                App.logger.writeLine(
                    logIdentifier,
                    "Received message: '$messagePlain'"
                )

                try {
                    val message =
                        JSONObject(
                            messagePlain
                        )

                    if (isEnableRPC) {
                        discordRPC?.processRPCMessage(
                            message
                        )
                    }
                } catch (
                    e: Exception
                ) {
                    App.logger.writeLine(
                        logIdentifier,
                        "Failed to parse message! " +
                                "(JSON deserialization threw an exception)"
                    )

                    App.logger.writeException(
                        logIdentifier,
                        e
                    )
                }
            }
        } catch (
            e: Exception
        ) {
            App.logger.writeLine(
                logIdentifier,
                "Failed to handle log entry"
            )

            App.logger.writeException(
                logIdentifier,
                e
            )
        }
    }

    internal fun isInExperienceState(): Boolean {
        return isInExperience
    }

    internal fun getActivityData(): ActivityData? {
        return data
    }

    fun dispose() {
        val logIdentifier =
            "ActivityWatcher::dispose"

        if (isStopMonitoring) {
            return
        }

        isStopMonitoring =
            true

        App.logger.writeLine(
            logIdentifier,
            "Disposing activity watcher session"
        )

        try {
            if (isEnableRPC) {
                discordRPC?.dispose()
            }
        } catch (
            e: Exception
        ) {
            App.logger.writeLine(
                logIdentifier,
                "Failed to dispose Discord RPC."
            )

            App.logger.writeException(
                logIdentifier,
                e
            )
        }

        App.appContext?.let {
            NotifyIconWrapper
                .hideConnectionNotification(
                    it
                )
        }

        if (isEnableRPC) {
            App.appContext?.let {
                NotifyIconWrapper
                    .hideDiscordRpcVisibilityNotification(
                        it
                    )
            }
        }

        watcherScope.cancel()
        if (!executor.isShutdown) {
            executor.shutdownNow()
        }
    }

    fun runWatcher() {
        val logIdentifier =
            "ActivityWatcher::runWatcher"

        if (isStopMonitoring) {
            App.logger.writeLine(
                logIdentifier,
                "Cannot start activity watcher because " +
                        "it has been disposed."
            )

            return
        }

        if (executor.isShutdown) {
            App.logger.writeLine(
                logIdentifier,
                "Cannot start activity watcher because executor is shut down."
            )

            return
        }

        App.logger.writeLine(
            logIdentifier,
            "Starting activity watcher session"
        )

        try {
            executor.submit {
                start()
            }
        } catch (
            e: Exception
        ) {
            App.logger.writeLine(
                logIdentifier,
                "Failed to submit activity watcher task."
            )

            App.logger.writeException(
                logIdentifier,
                e
            )
        }
    }
}