package com.chevstrap.rbx.UI.ViewModels.Settings;

import com.chevstrap.rbx.App;
import com.chevstrap.rbx.ConfigManager;
import com.chevstrap.rbx.Enums.RobloxAppType;
import com.chevstrap.rbx.R;
import com.chevstrap.rbx.UI.Frontend;

import java.util.Map;
import java.util.Objects;

public class BehaviourViewModel {
    public BehaviourViewModel() {}

    public boolean isEnableBringToLatestUpdate() {
        return App.getChevstrapSettings().isManualUpdateBootstrapper();
    }

    public void setEnableBringToLatestUpdate(boolean value) {
        App.getChevstrapSettings().setManualUpdateBootstrapper(value);
    }

    public String get_preferred_roblox_app() {
        String value = App.getChevstrapSettings().getPreferredRobloxApp();
        for (Map.Entry<RobloxAppType, String> entry : ConfigManager.RobloxAppTypes.entrySet()) {
            if (Objects.equals(entry.getValue(), value)) {
                return entry.getKey().name();
            }
        }
        return ConfigManager.RobloxAppTypes.keySet().iterator().next().name();
    }

    public void set_preferred_roblox_app(String modeName) {
        if (modeName == null) {
            return;
        }

        try {
            RobloxAppType mode = RobloxAppType.valueOf(modeName);
            App.getChevstrapSettings().setPreferredRobloxApp(ConfigManager.RobloxAppTypes.get(mode));
        } catch (IllegalArgumentException e) {
            App.getChevstrapSettings().setPreferredRobloxApp("global");
        }
    }
}