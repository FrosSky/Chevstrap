package com.chevstrap.rbx.enums

import com.chevstrap.rbx.App
import com.chevstrap.rbx.R
import com.chevstrap.rbx.extensions.ResourceManagerEx

enum class ThemeRecreated(@JvmField val displayName: String) {
    Dark(
        ResourceManagerEx.getStringOrEmpty(
            App.appContext!!,
            R.string.enums_presets_settings_app_theme_in_app_dark
        )
    ),
    Light(
        ResourceManagerEx.getStringOrEmpty(
            App.appContext!!,
            R.string.enums_presets_settings_app_theme_in_app_light
        )
    );

}