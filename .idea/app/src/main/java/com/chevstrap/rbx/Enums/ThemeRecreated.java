package com.chevstrap.rbx.Enums;

import com.chevstrap.rbx.App;
import com.chevstrap.rbx.R;

public enum ThemeRecreated {
    dark(App.getTextLocale(App.getAppContext(), R.string.enums_flagpresets_chevstrap_settings_apptheme_dark)),
    light(App.getTextLocale(App.getAppContext(), R.string.enums_flagpresets_chevstrap_settings_apptheme_light));

    private final String displayName;

    ThemeRecreated(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}