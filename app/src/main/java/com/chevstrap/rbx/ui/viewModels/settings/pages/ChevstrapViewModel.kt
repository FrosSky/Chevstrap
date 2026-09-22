package com.chevstrap.rbx.ui.viewModels.settings.pages

import com.chevstrap.rbx.App
import com.chevstrap.rbx.ConfigManager
import com.chevstrap.rbx.R
import com.chevstrap.rbx.SettingsActivity
import com.chevstrap.rbx.enums.ThemeRecreated
import com.chevstrap.rbx.extensions.ResourceManagerEx
import com.chevstrap.rbx.ui.Frontend
import com.chevstrap.rbx.ui.views.customDialogs.LogExplorerFragment
import kotlin.collections.iterator

class ChevstrapViewModel {

    var appThemeInApp: String
        get() {
            val value = App.config.data.appThemeInApp

            for ((mode, theme) in ConfigManager.ThemeRecreates) {
                if (theme == value) {
                    return mode.name
                }
            }

            return ConfigManager.ThemeRecreates.keys
                .firstOrNull()
                ?.name
                ?: ThemeRecreated.Dark.name
        }

        set(modeName) {
            try {
                val mode = ThemeRecreated.valueOf(modeName)
                App.config.data.appThemeInApp =
                    ConfigManager.ThemeRecreates[mode] ?: "dark"

                App.config.checkForChanges()
            } catch (_: IllegalArgumentException) {
                App.config.data.appThemeInApp = "dark"
            }
        }

    fun importCustomAppBackground() {
        val settingsActivity = App.savedSettingsActivity

        if (settingsActivity is SettingsActivity) {
            if (App.config.data.backgroundImageUri.isNotEmpty()) {
                Frontend.showMessageBoxWithRunnable(
                    App.savedFragmentActivity,
                    ResourceManagerEx.getStringOrEmpty(App.appContext, R.string.dialog_remove_custom_background),
                    true,
                    {
                        App.config.data.backgroundImageUri = ""
                        settingsActivity.removeBackgroundImage()
                    },
                    {}
                )
            } else {
                settingsActivity.imagePickerLauncher.launch("image/*")
            }
        }
    }

    fun logExplorer() {
        val settingsActivity = App.savedSettingsActivity

        if (settingsActivity is SettingsActivity) {
            LogExplorerFragment().show(
                settingsActivity.supportFragmentManager,
                "LogExplorerDialog"
            )
        }
    }

}