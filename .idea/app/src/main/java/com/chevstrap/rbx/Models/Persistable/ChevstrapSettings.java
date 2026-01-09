package com.chevstrap.rbx.Models.Persistable;

import java.util.LinkedHashMap;
import java.util.Map;

public class ChevstrapSettings {
    private static ChevstrapSettings instance;
    private boolean manual_update_bootstrapper = true;
    private String preferred_roblox_app = "global";
    private String locale = "nil";
    private int graphics_quality = 0;
    private String custom_roblox_client_package = "";
    private boolean use_fflags_manager = true;
    private boolean use_device_prefs = true;
    private boolean server_location_indicator_enabled = false;
    private boolean activity_tracker_enabled = false;
    private String app_theme_in_app = "dark";
    private String display_video_theme_with_uri = "";
    private boolean set_in_game_reducedmotion = false;
    private boolean set_in_game_haptics = true;
    private boolean force_potrait_mode = false;
    private final Map<String, Object> settingsMap = new LinkedHashMap<>();

    private ChevstrapSettings() {
        settingsMap.put("graphics_quality", graphics_quality);
        settingsMap.put("manual_update_bootstrapper", manual_update_bootstrapper);
        settingsMap.put("preferred_roblox_app", preferred_roblox_app);
        settingsMap.put("locale", locale);
        settingsMap.put("custom_roblox_client_package", custom_roblox_client_package);
        settingsMap.put("use_device_prefs", use_fflags_manager);
        settingsMap.put("use_fflags_manager", use_fflags_manager);
        settingsMap.put("server_location_indicator_enabled", server_location_indicator_enabled);
        settingsMap.put("activity_tracker_enabled", activity_tracker_enabled);
        settingsMap.put("app_theme_in_app", app_theme_in_app);
        settingsMap.put("set_in_game_reducedmotion", set_in_game_reducedmotion);
        settingsMap.put("set_in_game_haptics", set_in_game_haptics);
        settingsMap.put("display_video_theme_with_uri", display_video_theme_with_uri);
        settingsMap.put("force_potrait_mode", force_potrait_mode);
    }

    public static synchronized ChevstrapSettings getInstance() {
        if (instance == null) {
            instance = new ChevstrapSettings();
        }
        return instance;
    }

    public Map<String, Object> asMap() {
        return settingsMap;
    }

    public void setFromMap(String key, Object value) {
        settingsMap.put(key, value);
        switch (key) {
            case "graphics_quality": graphics_quality = (Integer) value; break;
            case "manual_update_bootstrapper": manual_update_bootstrapper = (Boolean) value; break;
            case "preferred_roblox_app": preferred_roblox_app = (String) value; break;
            case "locale": locale = (String) value; break;
            case "custom_roblox_client_package": custom_roblox_client_package = (String) value; break;
            case "use_device_prefs": use_device_prefs = (Boolean) value; break;
            case "use_fflags_manager": use_fflags_manager = (Boolean) value; break;
            case "server_location_indicator_enabled": server_location_indicator_enabled = (Boolean) value; break;
            case "activity_tracker_enabled": activity_tracker_enabled = (Boolean) value; break;
            case "app_theme_in_app": app_theme_in_app = (String) value; break;
            case "set_in_game_reducedmotion": set_in_game_reducedmotion = (Boolean) value; break;
            case "set_in_game_haptics": set_in_game_haptics = (Boolean) value; break;
            case "display_video_theme_with_uri": display_video_theme_with_uri = (String) value; break;
            case "force_potrait_mode": force_potrait_mode = (Boolean) value; break;
        }
    }

    public int getSelectedGraphicsQuality() { return graphics_quality; }
    public void setSelectedGraphicsQuality(int val) { setFromMap("graphics_quality", val); }
    public boolean isManualUpdateBootstrapper() { return manual_update_bootstrapper; }
    public void setManualUpdateBootstrapper(boolean val) { setFromMap("manual_update_bootstrapper", val); }
    public String getPreferredRobloxApp() { return preferred_roblox_app; }
    public void setPreferredRobloxApp(String val) { setFromMap("preferred_roblox_app", val); }
    public String getLocale() { return locale; }
    public void setLocale(String val) { setFromMap("locale", val); }
    public String getCustomRobloxClientPackage() { return custom_roblox_client_package; }
    public void setCustomRobloxClientPackage(String val) { setFromMap("custom_roblox_client_package", val); }
    public String getDisplayVideoThemeWithUri() { return display_video_theme_with_uri; }
    public void setDisplayVideoThemeWithUri(String val) { setFromMap("display_video_theme_with_uri", val); }
    public boolean isUseFFlagsManager() { return use_fflags_manager; }
    public void setUseFastFlagManager(boolean val) { setFromMap("use_fflags_manager", val); }
    public boolean isUseDevicePrefs() { return use_device_prefs; }
    public void setUseDevicePrefs(boolean val) { setFromMap("use_device_prefs", val); }
    public boolean isServerLocationIndicatorEnabled() { return server_location_indicator_enabled; }
    public void setServerLocationIndicatorEnabled(boolean val) { setFromMap("server_location_indicator_enabled", val); }
    public boolean isActivityTrackerEnabled() { return activity_tracker_enabled; }
    public void setActivityTrackerEnabled(boolean val) { setFromMap("activity_tracker_enabled", val); }
    public String getAppThemeInApp() { return app_theme_in_app; }
    public void setAppThemeInApp(String val) { setFromMap("app_theme_in_app", val); }
    public boolean isSetInGameReducedMotion() { return set_in_game_reducedmotion; }
    public void setSetInGameReducedMotion(boolean val) { setFromMap("set_in_game_reducedmotion", val); }
    public boolean isSetInGameHaptics() { return set_in_game_haptics; }
    public void setSetInGameHaptics(boolean val) { setFromMap("set_in_game_haptics", val); }
    public boolean isForcePotraitMode() { return force_potrait_mode; }
    public void setForcePotraitMode(boolean val) { setFromMap("force_potrait_mode", val); }
}
