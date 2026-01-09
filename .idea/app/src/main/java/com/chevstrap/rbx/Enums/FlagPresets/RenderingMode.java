package com.chevstrap.rbx.Enums.FlagPresets;

import com.chevstrap.rbx.App;
import com.chevstrap.rbx.R;

public enum RenderingMode {
    Automatic(App.getTextLocale(App.getAppContext(), R.string.common_automatic)),
    OpenGL(App.getTextLocale(App.getAppContext(), R.string.enums_flagpresets_renderingmode_opengl)),
    Vulkan(App.getTextLocale(App.getAppContext(), R.string.enums_flagpresets_renderingmode_vulkan));

    private final String displayName;

    RenderingMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
