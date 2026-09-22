package com.chevstrap.rbx.enums

import com.chevstrap.rbx.App
import com.chevstrap.rbx.R
import com.chevstrap.rbx.extensions.ResourceManagerEx

enum class DiscordStatusType(@JvmField val displayName: String) {
    Online(
        ResourceManagerEx.getStringOrEmpty(
            App.appContext!!,
            R.string.enums_presets_integrations_discord_online_status_online
        )
    ),
    Idle(
        ResourceManagerEx.getStringOrEmpty(
            App.appContext!!,
            R.string.enums_presets_integrations_discord_online_status_idle
        )
    ),
    DoNotDisturb(
        ResourceManagerEx.getStringOrEmpty(
            App.appContext!!,
            R.string.enums_presets_integrations_discord_online_status_do_not_disturb
        )
    );
}