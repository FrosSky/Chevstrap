package com.chevstrap.rbx.Enums.PrefPresets;

import com.chevstrap.rbx.App;
import com.chevstrap.rbx.R;

public enum VolumeMode {
    Automatic(App.getTextLocale(App.getAppContext(), R.string.common_automatic)),
    Level1(App.getTextLocale(App.getAppContext(), R.string.common_level1)),
    Level2(App.getTextLocale(App.getAppContext(), R.string.common_level2)),
    Level3(App.getTextLocale(App.getAppContext(), R.string.common_level3)),
    Level4(App.getTextLocale(App.getAppContext(), R.string.common_level4)),
    Level5(App.getTextLocale(App.getAppContext(), R.string.common_level5)),
    Level6(App.getTextLocale(App.getAppContext(), R.string.common_level6)),
    Level7(App.getTextLocale(App.getAppContext(), R.string.common_level7)),
    Level8(App.getTextLocale(App.getAppContext(), R.string.common_level8)),
    Level9(App.getTextLocale(App.getAppContext(), R.string.common_level9)),
    Level10(App.getTextLocale(App.getAppContext(), R.string.common_level10));

    private final String displayName;

    VolumeMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}