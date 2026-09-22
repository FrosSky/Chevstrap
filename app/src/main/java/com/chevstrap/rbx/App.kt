package com.chevstrap.rbx

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.fragment.app.FragmentActivity
import com.chevstrap.rbx.models.persistable.ConfigData
import com.chevstrap.rbx.utility.HTTPFetcher
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import org.json.JSONObject
import java.lang.ref.WeakReference

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        confManager = ConfigManager.instance

        onStartup()
    }

    interface ReleaseCallback {
        fun onSuccess(version: String?)
        fun onError(e: Exception?)
    }

    companion object {
        private var confManager: ConfigManager? = null
        const val PROJECT_NAME = "Chevstrap"
        const val PROJECT_REPOSITORY = "FrosSky/Chevstrap"
        const val PROJECT_DOWNLOAD_LINK =
            "https://github.com/FrosSky/Chevstrap/releases/latest"
        const val DISCORD_SERVER_INVITE_LINK =
            "https://discord.gg/2UcmM842h4"
        const val PROJECT_WIKI =
            "https://github.com/FrosSky/Chevstrap/wiki"

        private var _isSmallMode = false
        private var _discordRichPresenceVisibility = false
        private var _userData = ""
        private val SmallModeListeners = mutableSetOf<(Boolean) -> Unit>()
        private val DiscordRichPresenceVisibilityListeners = mutableSetOf<(Boolean) -> Unit>()
        var bootstrapper: Bootstrapper? = null

        @JvmStatic
        var isSmallMode: Boolean
            get() = _isSmallMode
            set(value) {
                _isSmallMode = value

                SmallModeListeners.toList().forEach { listener ->
                    listener(value)
                }
            }

        @JvmStatic
        var userData: String
            get() = _userData
            set(value) {
                _userData = value
            }

        @JvmStatic
        fun addSmallModeListener(listener: (Boolean) -> Unit) {
            SmallModeListeners.add(listener)
        }

        @JvmStatic
        fun removeSmallModeListener(listener: (Boolean) -> Unit) {
            SmallModeListeners.remove(listener)
        }

        @JvmStatic
        var appContext: Context? = null
            private set

        var savedFragmentActivity: FragmentActivity?
            get() = savedFragmentActivityRef?.get()
            set(value) {
                savedFragmentActivityRef =
                    if (value == null) null
                    else WeakReference(value)
            }

        private var savedSettingsActivityRef: WeakReference<Activity>? = null
        private var savedMainActivityRef: WeakReference<Activity>? = null
        private var savedFragmentActivityRef: WeakReference<FragmentActivity>? = null

        @Volatile
        var isAlreadyStartup = false
            private set
        @JvmField
        var isLastLogFoundOrMaybeNot = false
        @JvmStatic
        val config: ConfigManager
            get() {
                if (confManager == null) {
                    confManager = ConfigManager.instance
                }
                return confManager!!
            }

        @JvmStatic
        var savedSettingsActivity: Activity?
            get() = savedSettingsActivityRef?.get()
            set(value) {
                savedSettingsActivityRef =
                    if (value == null) null
                    else WeakReference(value)
            }

        @JvmStatic
        var savedMainActivity: Activity?
            get() = savedMainActivityRef?.get()
            set(value) {
                savedMainActivityRef =
                    if (value == null) null
                    else WeakReference(value)
            }

        private fun initializeLogger() {
            Logger.instance.initializePersistent()
        }

        @Synchronized
        @JvmStatic
        fun onStartup() {
            if (isAlreadyStartup)
                return

            try {
                initializeLogger()
                val logger = Logger.instance
                logger.writeLine(
                    "App::onStartup",
                    "Starting $PROJECT_NAME 2.0"
                )

                logger.writeLine(
                    "App::onStartup",
                    "Android ${Build.VERSION.RELEASE} " +
                            "(SDK ${Build.VERSION.SDK_INT}, ${Build.VERSION.CODENAME})"
                )

                config.load(false)
                isAlreadyStartup = true

            } catch (e: Exception) {
                e.printStackTrace()

                try {
                    Logger.instance.writeLine(
                        "App::onStartup",
                        "Startup failed: ${e.stackTraceToString()}"
                    )
                } catch (_: Exception) {
                }
            }
        }

        @JvmStatic
        fun getLatestReleaseAsync(callback: ReleaseCallback) {
            Thread {
                try {
                    val response = HTTPFetcher.getJson(
                        PROJECT_DOWNLOAD_LINK
                    )

                    val json = JSONObject(response.toString())

                    if (!json.has("tag_name"))
                        throw Exception("Invalid GitHub response.")

                    val version = json
                        .getString("tag_name")
                        .removePrefix("v")

                    callback.onSuccess(version)

                } catch (e: Exception) {
                    callback.onError(e)
                }
            }.start()
        }

        @JvmStatic
        fun getCurrentVersion(context: Context?): String? {
            if (context == null)
                return null
            return try {
                val info =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        context.packageManager.getPackageInfo(
                            context.packageName,
                            PackageManager.PackageInfoFlags.of(0)
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        context.packageManager.getPackageInfo(
                            context.packageName,
                            0
                        )
                    }
                info.versionName
            } catch (_: Exception) {
                null
            }
        }

        private fun isNetworkAvailable(): Boolean {
            val context = appContext ?: return true
            val connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return true
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }

        @JvmStatic
        val logger: Logger
            get() = Logger.instance
    }
}