package com.chevstrap.rbx

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.FragmentManager
import com.chevstrap.rbx.enums.LaunchMode
import com.chevstrap.rbx.extensions.ResourceManagerEx
import com.chevstrap.rbx.ui.Frontend

class LaunchHandler(private val launchSettings: LaunchSettings) {

    private var fragmentManager: FragmentManager? = null

    fun setFragmentManager(
        fragmentManager: FragmentManager?
    ) {
        this.fragmentManager = fragmentManager
    }

    fun run() {
        val logIdentifier = "LaunchHandler::run"

        App.logger.writeLine(
            logIdentifier,
            "Running launcher"
        )

        App.logger.writeLine(
            logIdentifier,
            "Launch mode: ${launchSettings.launchMode}"
        )

        App.logger.writeLine(
            logIdentifier,
            "Menu: ${launchSettings.menu}"
        )

        App.logger.writeLine(
            logIdentifier,
            "Watcher: ${launchSettings.watcher}"
        )

        App.logger.writeLine(
            logIdentifier,
            "Background updater: ${launchSettings.backgroundUpdater}"
        )

        App.logger.writeLine(
            logIdentifier,
            "Quiet: ${launchSettings.quiet}"
        )

        App.logger.writeLine(
            logIdentifier,
            "Launch arguments: ${launchSettings.launchArgs}"
        )

        processLaunchArgs()
    }

    fun processLaunchArgs() {
        val logIdentifier = "LaunchHandler::processLaunchArgs"

        when {
            launchSettings.uninstall -> {
                App.logger.writeLine(
                    logIdentifier,
                    "Opening uninstaller"
                )

                launchUninstaller()
            }

            launchSettings.menu -> {
                App.logger.writeLine(
                    logIdentifier,
                    "Opening settings"
                )

                launchSettingsScreen()
            }

            launchSettings.watcher -> {
                App.logger.writeLine(
                    logIdentifier,
                    "Opening watcher"
                )

                launchWatcher()
            }

            launchSettings.backgroundUpdater -> {
                App.logger.writeLine(
                    logIdentifier,
                    "Opening background updater"
                )

                launchBackgroundUpdater()
            }

            launchSettings.launchMode in LaunchMode.entries -> {
                App.logger.writeLine(
                    logIdentifier,
                    "Opening bootstrapper (${launchSettings.launchMode})"
                )

                launchRoblox(
                    launchSettings.launchMode
                )
            }

            else -> {
                App.logger.writeLine(
                    logIdentifier,
                    "Closing - quiet flag active"
                )
            }
        }
    }

    private fun launchSettingsScreen() {
        val logIdentifier = "LaunchHandler::launchSettingsScreen"

        val context = App.appContext ?: return
        try {
            val intent = Intent(
                context,
                SettingsActivity::class.java
            ).apply {
                putExtra(
                    "open_settings",
                    true
                )

                if (context !is Activity) {
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }
            }
            context.startActivity(intent)
            App.savedMainActivity?.finish()
        } catch (e: Exception) {
            App.logger.writeLine(
                logIdentifier,
                "Failed to open settings"
            )

            App.logger.writeException(
                logIdentifier,
                e
            )
        }
    }

    private fun launchRoblox(
        launchMode: LaunchMode
    ) {
        val logIdentifier = "LaunchHandler::launchRoblox"

        App.logger.writeLine(
            logIdentifier,
            "Initializing bootstrapper"
        )

        val bootstrapper = Bootstrapper(
            launchMode
        )

        bootstrapper.setFragmentManager(
            fragmentManager
        )

        App.bootstrapper = bootstrapper

        Thread {
            try {
                bootstrapper.run()

                App.logger.writeLine(
                    logIdentifier,
                    "Bootstrapper task has finished"
                )

            } catch (e: Exception) {
                App.logger.writeLine(
                    logIdentifier,
                    "An exception occurred when running the bootstrapper"
                )

                App.logger.writeException(
                    logIdentifier,
                    e
                )

                val context = App.appContext
                    ?: return@Thread

                val activity = App.savedFragmentActivity
                    ?: return@Thread

                activity.runOnUiThread {
                    try {
                        if (
                            activity.isFinishing ||
                            activity.isDestroyed
                        ) {
                            return@runOnUiThread
                        }

                        Frontend.showExceptionDialog(
                            activity,
                            ResourceManagerEx.getStringOrEmpty(
                                context,
                                R.string.dialog_failed_to_launch
                            ),
                            e
                        )

                    } catch (dialogException: Exception) {
                        App.logger.writeLine(
                            logIdentifier,
                            "Failed to show bootstrapper exception dialog"
                        )

                        App.logger.writeException(
                            logIdentifier,
                            dialogException
                        )
                    }
                }

            } finally {
                App.logger.writeLine(
                    logIdentifier,
                    "Bootstrapper thread finished"
                )
            }
        }.start()

        App.logger.writeLine(
            logIdentifier,
            "Exiting"
        )
    }

    private fun launchWatcher() {
        val logIdentifier = "LaunchHandler::launchWatcher"

        App.logger.writeLine(
            logIdentifier,
            "Starting watcher"
        )

        try {
            CustomWatcher.getInstance().run()

        } catch (e: Exception) {
            App.logger.writeLine(
                logIdentifier,
                "Failed to start watcher"
            )

            App.logger.writeException(
                logIdentifier,
                e
            )
        }
    }

    private fun launchBackgroundUpdater() {
        val logIdentifier =
            "LaunchHandler::launchBackgroundUpdater"

        App.logger.writeLine(
            logIdentifier,
            "Initializing background updater"
        )

        launchRoblox(
            launchSettings.launchMode
        )
    }

    private fun launchUninstaller() {
        val logIdentifier =
            "LaunchHandler::launchUninstaller"

        App.logger.writeLine(
            logIdentifier,
            "Uninstaller is not supported on Android"
        )
    }
}