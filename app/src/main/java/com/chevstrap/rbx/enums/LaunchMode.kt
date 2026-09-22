package com.chevstrap.rbx.enums

import com.chevstrap.rbx.App
import com.chevstrap.rbx.R
import com.chevstrap.rbx.extensions.ResourceManagerEx

enum class LaunchMode(@JvmField val displayName: String) {
    Global(ResourceManagerEx.getStringOrEmpty(App.appContext!!, R.string.enums_presets_behaviour_roblox_global)),
    VNG(ResourceManagerEx.getStringOrEmpty(App.appContext!!, R.string.enums_presets_behaviour_roblox_vng)),
    GalaxyStore(ResourceManagerEx.getStringOrEmpty(App.appContext!!, R.string.enums_presets_behaviour_roblox_galaxy_store));

}