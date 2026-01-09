package com.chevstrap.rbx.Enums.FlagPresets;

import com.chevstrap.rbx.App;
import com.chevstrap.rbx.R;

public enum TextureMode {
    Automatic(App.getTextLocale(App.getAppContext(), R.string.common_automatic)),
    x0(App.getTextLocale(App.getAppContext(), R.string.common_lowest)),
    x1(App.getTextLocale(App.getAppContext(), R.string.common_low)),
    x2(App.getTextLocale(App.getAppContext(), R.string.common_medium)),
    x3(App.getTextLocale(App.getAppContext(), R.string.common_high));

    private final String displayName;

    TextureMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
