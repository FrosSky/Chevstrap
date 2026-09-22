package com.chevstrap.rbx

import com.chevstrap.rbx.enums.LaunchMode

class LaunchSettings(
    args: Array<String>
) {

    companion object {
        private const val LOG_IDENT = "LaunchSettings::LaunchSettings"
    }

    var menu: Boolean = false
        private set

    var watcher: Boolean = false
        private set

    var backgroundUpdater: Boolean = false
        private set

    var quiet: Boolean = false
        private set

    var uninstall: Boolean = false
        private set

    var noLaunch: Boolean = false
        private set

    var player: Boolean = false
        private set

    var launchMode: LaunchMode = LaunchMode.Global
        private set

    var launchArgs: String = ""
        private set

    var backgroundUpdaterData: String? = null
        private set

    var playerData: String? = null
        private set

    init {
        launchMode = getPreferredLaunchMode()

        App.logger.writeLine(
            LOG_IDENT,
            "Launched with arguments: ${args.joinToString(" ")}"
        )

        parseArguments(args)
        App.logger.writeLine(LOG_IDENT, "Launch mode: $launchMode")
        App.logger.writeLine(LOG_IDENT, "Launch arguments: $launchArgs")
        App.logger.writeLine(LOG_IDENT, "Menu: $menu")
        App.logger.writeLine(LOG_IDENT, "Watcher: $watcher")
        App.logger.writeLine(LOG_IDENT, "Background updater: $backgroundUpdater")
        App.logger.writeLine(LOG_IDENT, "Quiet: $quiet")
    }

    private fun parseArguments(args: Array<String>) {
        var startIndex = 0

        if (args.isNotEmpty()) {
            val firstArgument = args[0]

            if (firstArgument.startsWith("roblox:", ignoreCase = true) ||
                firstArgument.startsWith("roblox-client:", ignoreCase = true)
            ) {
                App.logger.writeLine(LOG_IDENT, "Got Roblox player argument")

                launchMode = getPreferredLaunchMode()
                launchArgs = firstArgument
                playerData = firstArgument
                player = true

                startIndex = 1
            }
        }

        var i = startIndex

        while (i < args.size) {
            val argument = args[i]

            if (!argument.startsWith("-")) {
                App.logger.writeLine(LOG_IDENT, "Invalid argument: $argument")
                i++
                continue
            }

            when (val identifier = argument.removePrefix("-").lowercase()) {
                "preferences", "menu", "settings" -> {
                    if (menu) logDuplicate(identifier) else {
                        menu = true
                        logFlag(identifier)
                    }
                }

                "watcher" -> {
                    if (watcher) logDuplicate(identifier) else {
                        watcher = true
                        logFlag(identifier)
                    }
                }

                "backgroundupdater" -> {
                    if (backgroundUpdater) logDuplicate(identifier) else {
                        backgroundUpdater = true
                        val data = getNextArgument(args, i)

                        if (data != null) {
                            backgroundUpdaterData = data.value
                            i = data.index
                            logFlagWithData(identifier, backgroundUpdaterData)
                        } else {
                            logFlag(identifier)
                        }
                    }
                }

                "quiet" -> {
                    if (quiet) logDuplicate(identifier) else {
                        quiet = true
                        logFlag(identifier)
                    }
                }

                "uninstall" -> {
                    if (uninstall) logDuplicate(identifier) else {
                        uninstall = true
                        logFlag(identifier)
                    }
                }

                "nolaunch" -> {
                    if (noLaunch) logDuplicate(identifier) else {
                        noLaunch = true
                        logFlag(identifier)
                    }
                }

                "client" -> {
                    if (player) logDuplicate(identifier) else {
                        player = true
                        launchMode = getPreferredLaunchMode()

                        val data = getNextArgument(args, i)
                        if (data != null) {
                            playerData = data.value
                            launchArgs = data.value
                            i = data.index
                            logFlagWithData(identifier, playerData)
                        } else {
                            logFlag(identifier)
                        }
                    }
                }

                else -> {
                    App.logger.writeLine(LOG_IDENT, "Unknown argument: $identifier")
                }
            }

            i++
        }
    }

    private fun getNextArgument(args: Array<String>, currentIndex: Int): ArgumentData? {
        val nextIndex = currentIndex + 1
        if (nextIndex >= args.size) return null

        val nextArgument = args[nextIndex]
        if (nextArgument.startsWith("-")) return null

        return ArgumentData(value = nextArgument, index = nextIndex)
    }

    private fun getPreferredLaunchMode(): LaunchMode {
        return try {
            when (App.config.data.preferredRobloxApp.lowercase()) {
                "vng" -> LaunchMode.VNG
                "galaxy_store" -> LaunchMode.GalaxyStore
                "global" -> LaunchMode.Global
                else -> LaunchMode.Global
            }
        } catch (_: Exception) {
            LaunchMode.Global
        }
    }

    private fun logFlag(identifier: String) {
        App.logger.writeLine(LOG_IDENT, "Identifier '$identifier' is active")
    }

    private fun logFlagWithData(identifier: String, data: String?) {
        App.logger.writeLine(LOG_IDENT, "Identifier '$identifier' is active with data: $data")
    }

    private fun logDuplicate(identifier: String) {
        App.logger.writeLine(LOG_IDENT, "Tried to set $identifier flag twice")
    }

    private data class ArgumentData(
        val value: String,
        val index: Int
    )
}