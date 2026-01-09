package com.chevstrap.rbx.UI.ViewModels.Settings;

import com.chevstrap.rbx.App;
import com.chevstrap.rbx.ConfigManager;
import com.chevstrap.rbx.Enums.PrefPresets.GraphicsQualityMode;

import java.util.Map;
import java.util.Objects;

public class LocalPreferencesViewModel {
    public LocalPreferencesViewModel() {}

    public boolean isUseDevicePrefs() {
        return App.getChevstrapSettings().isUseDevicePrefs();
    }

    public void setUseDevicePrefs(boolean value) {
        App.getChevstrapSettings().setUseDevicePrefs(value);
    }

    public boolean isReducedMotion() {
        return App.getChevstrapSettings().isSetInGameReducedMotion();
    }

    public void setReducedMotion(boolean value) {
        App.getChevstrapSettings().setSetInGameReducedMotion(value);
    }

    public boolean isInGameHaptics() {
        return App.getChevstrapSettings().isSetInGameHaptics();
    }

    public void setInGameHaptics(boolean value) {
        App.getChevstrapSettings().setSetInGameHaptics(value);
    }

    public String getSelectedGraphicsQuality() {
        Integer value = App.getChevstrapSettings().getSelectedGraphicsQuality();
        for (Map.Entry<GraphicsQualityMode, Integer> entry
                : ConfigManager.GraphicsQualityModes.entrySet()) {
            if (Objects.equals(entry.getValue(), value)) {
                return entry.getKey().name();
            }
        }
        return GraphicsQualityMode.Automatic.name();
    }

    public void setSelectedGraphicsQuality(String modeName) {
        try {
            GraphicsQualityMode mode = GraphicsQualityMode.valueOf(modeName);
            App.getChevstrapSettings().setSelectedGraphicsQuality(Integer.parseInt(modeName));
        } catch (IllegalArgumentException e) {
            App.getChevstrapSettings().setSelectedGraphicsQuality(1);
        }
    }
}