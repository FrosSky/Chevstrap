package com.chevstrap.rbx.ui.viewModels.settings.pages

import com.chevstrap.rbx.App
import com.chevstrap.rbx.ConfigManager
import com.chevstrap.rbx.enums.LaunchMode

class BehaviourViewModel {

    var bringToLatestUpdate: Boolean
        get() = App.config.data.manualUpdateBootstrapper
        set(value) {
            App.config.data.manualUpdateBootstrapper = value
        }

    var preferredRobloxApp: String
        get() {
            val value = App.config.data.preferredRobloxApp

            for (entry in ConfigManager.RobloxAppTypes.entries) {
                if (entry.value == value) {
                    return entry.key.name
                }
            }

            return ConfigManager.RobloxAppTypes.keys
                .firstOrNull()
                ?.name
                ?: LaunchMode.Global.name
        }

        set(modeName) {
            try {
                val mode = LaunchMode.valueOf(modeName)
                App.config.data.preferredRobloxApp =
                    ConfigManager.RobloxAppTypes[mode] ?: "global"
            } catch (_: IllegalArgumentException) {
                App.config.data.preferredRobloxApp = "global"
            }
        }
}