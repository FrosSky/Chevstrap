package com.chevstrap.rbx.ui.viewModels.settings.pages

import android.Manifest
import chevstrap.preference.PrefsManager
import com.chevstrap.rbx.App
import com.chevstrap.rbx.ConfigManager
import com.chevstrap.rbx.R
import com.chevstrap.rbx.SettingsActivity
import com.chevstrap.rbx.enums.DiscordStatusType
import com.chevstrap.rbx.extensions.ResourceManagerEx
import com.chevstrap.rbx.ui.Frontend
import com.chevstrap.rbx.ui.views.customDialogs.ServerHistoryFragment
import com.chevstrap.rbx.utility.AppPermissions
import kotlin.collections.iterator

class IntegrationsViewModel {
    var queryServerLocation: Boolean
        get() {
            if (AppPermissions.areDeclined(Manifest.permission.POST_NOTIFICATIONS)) {
                return false
            }

            return App.config.data.serverLocationIndicatorEnabled
        }
        set(value) {
            if (!AppPermissions.areGranted(Manifest.permission.POST_NOTIFICATIONS)) {
                requestNotificationPermission()
            } else {
                App.config.data.serverLocationIndicatorEnabled = value
            }

            if (AppPermissions.areDeclined(Manifest.permission.POST_NOTIFICATIONS)) {
                Frontend.showMessageBox(
                    App.savedFragmentActivity,
                    ResourceManagerEx.getStringOrEmpty(App.appContext, R.string.dialog_feature_requires_notification_permission)
                )
            }
        }

    var showGameActivity: Boolean
        get() = App.config.data.showGameActivity
        set(value) {
            if (isUserNotLoggedDiscord()) {
                showDialogUserNotLoggedDiscord()
                return
            }
            App.config.data.showGameActivity = value
        }

    var activityTrackerEnabled: Boolean
        get() = App.config.data.activityTrackerEnabled
        set(value) {
            App.config.data.activityTrackerEnabled = value
        }

    var showRobloxAccount: Boolean
        get() = App.config.data.showRobloxAccount
        set(value) {
            if (isUserNotLoggedDiscord()) {
                showDialogUserNotLoggedDiscord()
                return
            }
            App.config.data.showRobloxAccount = value
        }

    var discordSetOnlineStatus: String
        get() {
            val value = App.config.data.discordSetOnlineStatus

            for ((mode, status) in ConfigManager.DiscordOnlineStatus) {
                if (status == value) {
                    return mode.name
                }
            }

            return ConfigManager.DiscordOnlineStatus.keys
                .firstOrNull()
                ?.name
                ?: DiscordStatusType.Online.name
        }

        set(modeName) {
            if (isUserNotLoggedDiscord()) {
                showDialogUserNotLoggedDiscord()
                return
            }
            try {
                val mode = DiscordStatusType.valueOf(modeName)
                App.config.data.discordSetOnlineStatus =
                    ConfigManager.DiscordOnlineStatus[mode] ?: "online"

            } catch (_: IllegalArgumentException) {
                App.config.data.discordSetOnlineStatus = "online"
            }
        }

    var allowActivityJoining: Boolean
        get() = App.config.data.allowActivityJoining
        set(value) {
            if (isUserNotLoggedDiscord()) {
                showDialogUserNotLoggedDiscord()
                return
            }
            App.config.data.allowActivityJoining = value
        }

    var discordAllowCustomPresence: Boolean
        get() = App.config.data.discordAllowCustomPresence
        set(value) {
            if (isUserNotLoggedDiscord()) {
                showDialogUserNotLoggedDiscord()
                return
            }
            App.config.data.discordAllowCustomPresence = value
        }

    var discordVerifiedCustomPresenceOnly: Boolean
        get() = App.config.data.discordVerifiedCustomPresenceOnly
        set(value) {
            if (isUserNotLoggedDiscord()) {
                showDialogUserNotLoggedDiscord()
                return
            }
            App.config.data.discordVerifiedCustomPresenceOnly = value
        }

    private fun isUserNotLoggedDiscord(): Boolean {
        return PrefsManager.getToken().isEmpty()
    }

    private fun showDialogUserNotLoggedDiscord() {
        Frontend.showMessageBox(
            App.savedFragmentActivity,
            ResourceManagerEx.getStringOrEmpty(App.appContext,  R.string.dialog_feature_requires_discord_login)
        )
    }

    fun serverHistoryDialog() {
        val settingsActivity = App.savedSettingsActivity

        if (settingsActivity is SettingsActivity) {
            ServerHistoryFragment().show(
                settingsActivity.supportFragmentManager,
                "ServerHistoryDialog"
            )
        }
    }

    fun myDiscordAccountPage() {
        val settingsActivity = App.savedSettingsActivity

        if (settingsActivity is SettingsActivity) {
            settingsActivity.movePage("Logged")
        }
    }

    private fun requestNotificationPermission() {
        val activity = App.savedSettingsActivity

        if (activity is SettingsActivity) {
            activity.requestNotificationPermission()
        }
    }
}