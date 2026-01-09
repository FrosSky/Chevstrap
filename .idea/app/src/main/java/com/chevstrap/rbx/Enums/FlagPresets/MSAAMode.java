package com.chevstrap.rbx.Enums.FlagPresets;

import com.chevstrap.rbx.App;
import com.chevstrap.rbx.R;

public enum MSAAMode {
    Automatic(App.getTextLocale(App.getAppContext(), R.string.common_automatic)),
    x1("1x"),
    x2("2x"),
    x4("4x");

    private final String displayName;

    MSAAMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}