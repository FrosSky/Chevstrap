package com.chevstrap.rbx.models.persistable

import kotlinx.serialization.Serializable

@Serializable
data class ConfigData(
    var activityTrackerEnabled: Boolean = false,
    var appThemeInApp: String = "dark",
    var backgroundImageUri: String = "",
    var showGameActivity: Boolean = false,
    var allowActivityJoining: Boolean = false,
    var showRobloxAccount: Boolean = false,
    var locale: String? = null,
    var manualUpdateBootstrapper: Boolean = true,
    var preferredRobloxApp: String = "global",
    var serverLocationIndicatorEnabled: Boolean = false,
    var discordSetOnlineStatus: String = "online",
    var discordAllowCustomPresence: Boolean = false,
    var discordVerifiedCustomPresenceOnly: Boolean = false,
)