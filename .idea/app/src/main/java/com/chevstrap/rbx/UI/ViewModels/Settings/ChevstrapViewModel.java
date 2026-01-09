package com.chevstrap.rbx.UI.ViewModels.Settings;

import com.chevstrap.rbx.App;
import com.chevstrap.rbx.ConfigManager;
import com.chevstrap.rbx.Enums.RobloxAppType;
import com.chevstrap.rbx.Enums.ThemeRecreated;

import java.util.Map;
import java.util.Objects;

public class ChevstrapViewModel {
    public ChevstrapViewModel() {}

    public String getapp_theme_in_app() {
        String value = App.getChevstrapSettings().getAppThemeInApp();
        for (Map.Entry<ThemeRecreated, String> entry : ConfigManager.ThemeRecreateds.entrySet()) {
            if (Objects.equals(entry.getValue(), value)) {
                return entry.getKey().name();
            }
        }
        return ConfigManager.ThemeRecreateds.keySet().iterator().next().name();
    }

    public void setapp_theme_in_app(String modeName) {
        if (modeName == null) {
            return;
        }

        try {
            ThemeRecreated mode = ThemeRecreated.valueOf(modeName);
            App.getChevstrapSettings().setAppThemeInApp(ConfigManager.ThemeRecreateds.get(mode));
        } catch (IllegalArgumentException e) {
            App.getChevstrapSettings().setAppThemeInApp("dark");
        }
    }

    public boolean isForcePotraitMode() {
        return App.getChevstrapSettings().isForcePotraitMode();
    }

    public void setForcePotraitMode(boolean value) {
        App.getChevstrapSettings().setForcePotraitMode(value);
    }

    public String getTheLocale() {
        try {
            return App.getChevstrapSettings().getLocale();
        } catch (Exception e) {
            return null;
        }
    }

    public void setTheLocale(String value) {
        App.getChevstrapSettings().setLocale(value);
    }
}