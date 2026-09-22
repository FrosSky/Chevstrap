package com.chevstrap.rbx

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.FragmentManager
import chevstrap.preference.PrefsManager
import com.chevstrap.rbx.appDirectories.RobloxClientData
import com.chevstrap.rbx.enums.LaunchMode
import com.chevstrap.rbx.extensions.ResourceManagerEx
import com.chevstrap.rbx.ui.Frontend
import com.chevstrap.rbx.ui.IBootstrapperDialog
import com.chevstrap.rbx.ui.viewModels.GlobalViewModel
import java.io.File
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.ZipFile

class Bootstrapper(
    private val launchMode: LaunchMode
) {

    private val cancelled = AtomicBoolean(false)
    private val handler = Handler(Looper.getMainLooper())

    private var fragmentManager: FragmentManager? = null
    private var packageName: String? = null
    private var dialog: IBootstrapperDialog? = null

    fun setFragmentManager(manager: FragmentManager?) {
        fragmentManager = manager
    }

    private fun createDialog(context: Context): IBootstrapperDialog? {
        return fragmentManager?.let { manager ->
            IBootstrapperDialog(
                context,
                manager
            ) { cancel() }
        }
    }

    fun cancel() {
        if (cancelled.compareAndSet(false, true)) {
            handler.post(::closeDialog)
        }
    }

    fun run() {
        val log = "Bootstrapper::run"

        try {
            val context = App.appContext ?: return cleanup()

            App.logger.writeLine(log, "Starting bootstrapper")
            App.logger.writeLine(log, "Launch mode: $launchMode")

            runCatching {
                App.config.load(false)
            }.onFailure {
                App.logger.writeException(log, it as Exception?)
            }

            packageName = runCatching {
                RobloxClientData().executablePackage
            }.onFailure {
                App.logger.writeException(log, it as Exception?)
            }.getOrNull()

            App.logger.writeLine(log, "Roblox package: $packageName")

            if (cancelled.get()) return cleanup()

            dialog = createDialog(context)
            showDialog()

            simulateProgress(0, 20)

            if (checkLauncherUpdate(context) ||
                cancelled.get() ||
                !checkRobloxInstalled(context)
            ) return

            if (isRobloxModified(context)) {
                showModifiedWarning(context)
                return
            }

            if (cancelled.get()) return cleanup()

            updateStatus(
                R.string.bootstrapper_status_applying_modifications,
                "50%"
            )

            simulateProgress(50, 70)
            if (cancelled.get() || !applyChanges()) return
            simulateProgress(70, 90)
            if (cancelled.get()) return cleanup()

            updateStatus(
                R.string.bootstrapper_status_starting_roblox,
                "90%"
            )

            simulateProgress(90, 100)

            if (!cancelled.get()) {
                safeSleep(300)
                launchRoblox(context)
            } else {
                cleanup()
            }

        } catch (e: Exception) {
            App.logger.writeException(log, e)
            cleanup()
        }
    }

    private fun showDialog() {
        if (cancelled.get()) return

        handler.post {
            if (cancelled.get()) return@post

            val current = dialog ?: return@post
            current.show()

            handler.post {
                if (cancelled.get() || dialog !== current) return@post

                current.updateStatus(
                    R.string.bootstrapper_status_connecting,
                    "0%"
                )
                current.setProgress("0%")
            }
        }
    }

    private fun checkLauncherUpdate(context: Context): Boolean {
        if (!App.config.data.manualUpdateBootstrapper || cancelled.get()) {
            return false
        }

        val current = runCatching {
            App.getCurrentVersion(context)
        }.getOrNull()

        if (current.isNullOrEmpty()) {
            App.logger.writeLine(
                "Bootstrapper::checkLauncherUpdate",
                "Failed to get current launcher version"
            )
            return false
        }

        return checkLatestLauncherVersion(context, current)
    }

    private fun checkLatestLauncherVersion(
        context: Context,
        currentVersion: String
    ): Boolean {
        val log = "Bootstrapper::checkLatestLauncherVersion"
        val latch = CountDownLatch(1)
        var updateAvailable = false

        App.getLatestReleaseAsync(
            object : App.ReleaseCallback {

                override fun onSuccess(version: String?) {
                    try {
                        if (!cancelled.get() && version != null) {
                            val current = currentVersion.removePrefix("v").trim()
                            val latest = version.removePrefix("v").trim()

                            App.logger.writeLine(
                                log,
                                "Current version: $current | Latest version: $latest"
                            )

                            updateAvailable = current != latest

                            if (updateAvailable) {
                                App.logger.writeLine(
                                    log,
                                    "Launcher update available: $current -> $latest"
                                )
                            } else {
                                App.logger.writeLine(
                                    log,
                                    "Launcher is up to date"
                                )
                            }
                        }
                    } finally {
                        latch.countDown()
                    }
                }

                override fun onError(e: Exception?) {
                    App.logger.writeLine(
                        log,
                        "Failed to get latest release"
                    )

                    e?.let {
                        App.logger.writeException(log, it)
                    }

                    latch.countDown()
                }
            }
        )

        try {
            latch.await(10, TimeUnit.SECONDS)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
            return false
        }

        if (cancelled.get() || !updateAvailable) return false

        handler.post {
            if (cancelled.get()) return@post

            GlobalViewModel.openWebpage(
                context,
                App.PROJECT_DOWNLOAD_LINK
            )

            updateStatus(
                R.string.bootstrapper_status_upgrading_chevstrap,
                "20%"
            )

            cancel()
        }

        return true
    }

    private fun checkRobloxInstalled(context: Context): Boolean {
        val pkg = packageName ?: return false

        return try {
            context.packageManager.getApplicationInfo(pkg, 0)
            App.logger.writeLine(
                "Bootstrapper::checkRobloxInstalled",
                "Roblox is installed"
            )
            true
        } catch (_: Exception) {
            App.logger.writeLine(
                "Bootstrapper::checkRobloxInstalled",
                "Roblox is not installed: $pkg"
            )
            showNotInstalled(context)
            false
        }
    }

    private fun isRobloxModified(context: Context): Boolean {
        val log = "Bootstrapper::isRobloxModified"
        val pkg = packageName ?: return false

        return try {
            val source = File(
                context.packageManager
                    .getApplicationInfo(pkg, 0)
                    .sourceDir
            )

            if (!source.exists()) return false

            ZipFile(source).use { zip ->
                Collections.list(zip.entries()).any {
                    it.name.startsWith("lib/")
                }.also {
                    if (it) {
                        App.logger.writeLine(
                            log,
                            "Roblox modification detected"
                        )
                    }
                }
            }
        } catch (e: Exception) {
            App.logger.writeLine(
                log,
                "Failed to check Roblox modification"
            )
            App.logger.writeException(log, e)
            false
        }
    }

    private fun applyChanges(): Boolean {
        val log = "Bootstrapper::applyChanges"

        return try {
            PrefsManager.setStatus(
                App.config.data.discordSetOnlineStatus
            )

            App.logger.writeLine(
                log,
                "Applying changes"
            )

            true
        } catch (e: Exception) {
            App.logger.writeLine(
                log,
                "Failed to apply changes"
            )
            App.logger.writeException(log, e)

            App.appContext?.let { context ->
                handler.post {
                    closeDialog()

                    Frontend.showExceptionDialog(
                        App.savedFragmentActivity,
                        getTextLocale(
                            context,
                            R.string.dialog_failed_to_apply_changes
                        ),
                        e
                    )
                }
            }

            false
        }
    }

    private fun launchRoblox(context: Context) {
        val log = "Bootstrapper::launchRoblox"
        val pkg = packageName

        App.logger.writeLine(log, "launchRoblox(): called")

        if (pkg.isNullOrEmpty()) {
            App.logger.writeLine(
                log,
                "launchRoblox(): package name is empty"
            )
            return cleanup()
        }

        App.logger.writeLine(
            log,
            "launchRoblox(): package=$pkg"
        )

        val intent = try {
            context.packageManager.getLaunchIntentForPackage(pkg)
        } catch (e: Exception) {
            App.logger.writeLine(
                log,
                "launchRoblox(): failed to get launch intent"
            )
            App.logger.writeException(log, e)
            cleanup()
            return
        }

        if (intent == null) {
            App.logger.writeLine(
                log,
                "launchRoblox(): launch intent is null"
            )
            showNotInstalled(context)
            return
        }

        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        handler.post {
            App.logger.writeLine(
                log,
                "launchRoblox(): INSIDE handler.post"
            )

            try {
                if (cancelled.get()) {
                    App.logger.writeLine(
                        log,
                        "launchRoblox(): cancelled before launch"
                    )
                    return@post
                }

                closeDialog()

                if (App.config.data.serverLocationIndicatorEnabled) {
                    try {
                        App.logger.writeLine(
                            log,
                            "launchRoblox(): starting CustomWatcher"
                        )

                        CustomWatcher.getInstance().run()

                        App.logger.writeLine(
                            log,
                            "launchRoblox(): CustomWatcher started"
                        )
                    } catch (e: Exception) {
                        App.logger.writeLine(
                            log,
                            "launchRoblox(): CustomWatcher failed"
                        )
                        App.logger.writeException(log, e)
                    }
                }

                App.logger.writeLine(
                    log,
                    "launchRoblox(): before startActivity"
                )

                context.startActivity(intent)

                App.logger.writeLine(
                    log,
                    "launchRoblox(): AFTER startActivity"
                )

            } catch (e: Exception) {
                App.logger.writeLine(
                    log,
                    "launchRoblox(): startActivity FAILED"
                )
                App.logger.writeException(log, e)

                runCatching {
                    Frontend.showPlayerErrorDialog(
                        App.savedFragmentActivity,
                        e
                    )
                }.onFailure {
                    App.logger.writeException(
                        log,
                        it as Exception?
                    )
                }

            } finally {
                cleanup()
            }
        }
    }

    private fun showNotInstalled(context: Context) {
        handler.post {
            closeDialog()

            Frontend.showMessageBox(
                App.savedFragmentActivity,
                getTextLocale(
                    context,
                    R.string.dialog_not_installed
                )
            )
        }
    }

    private fun showModifiedWarning(context: Context) {
        handler.post {
            closeDialog()

            Frontend.showMessageBox(
                App.savedFragmentActivity,
                getTextLocale(
                    context,
                    R.string.dialog_modded_roblox_warning
                )
            )
        }
    }

    private fun updateStatus(
        resId: Int,
        progress: String
    ) {
        handler.post {
            if (cancelled.get()) return@post

            dialog?.let {
                it.updateStatus(resId, progress)
                it.setProgress(progress)
            }
        }
    }

    private fun simulateProgress(
        start: Int,
        end: Int,
    ) {
        if (cancelled.get()) return

        val delay = 1000L / (end - start).coerceAtLeast(1)
        for (progress in start..end) {
            if (cancelled.get()) break

            handler.post {
                if (!cancelled.get()) {
                    dialog?.setProgress("$progress%")
                }
            }

            safeSleep(delay)
        }
    }

    private fun safeSleep(ms: Long) {
        try {
            Thread.sleep(ms)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    private fun closeDialog() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            dialog?.close()
            dialog = null
        } else {
            handler.post {
                dialog?.close()
                dialog = null
            }
        }
    }

    private fun cleanup() {
        handler.removeCallbacksAndMessages(null)
        closeDialog()

        fragmentManager = null
        packageName = null

        if (App.bootstrapper === this) {
            App.bootstrapper = null
        }
    }

    private fun getTextLocale(
        context: Context,
        resId: Int
    ): String {
        return ResourceManagerEx.getStringOrEmpty(
            context,
            resId
        )
    }
}