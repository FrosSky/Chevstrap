package com.chevstrap.rbx;

import android.util.Log;

import com.chevstrap.rbx.Enums.FlagPresets.*;
import com.chevstrap.rbx.Enums.PrefPresets.GraphicsQualityMode;
import com.chevstrap.rbx.Enums.PrefPresets.VolumeMode;
import com.chevstrap.rbx.Enums.RobloxAppType;
import com.chevstrap.rbx.Enums.ThemeRecreated;
import com.chevstrap.rbx.Models.Persistable.ChevstrapSettings;

import java.io.*;
import java.util.*;

public class ConfigManager extends JsonManager<Map<String, Object>> {
    private static ConfigManager instance;

    private ConfigManager() {
        this.prop = new LinkedHashMap<>();
        this.originalProp = new LinkedHashMap<>();
    }

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    public Map<String, Object> getProp() {
        return prop;
    }
    public void setProp(Map<String, Object> prop) {
        this.prop = prop;
    }

    public static final Map<GraphicsQualityMode, Integer> GraphicsQualityModes;
    static {
        Map<GraphicsQualityMode, Integer> map = new EnumMap<>(GraphicsQualityMode.class);
        map.put(GraphicsQualityMode.Automatic, 0);
        map.put(GraphicsQualityMode.Level1, 1);
        map.put(GraphicsQualityMode.Level2, 2);
        map.put(GraphicsQualityMode.Level3, 3);
        map.put(GraphicsQualityMode.Level4, 4);
        map.put(GraphicsQualityMode.Level5, 5);
        map.put(GraphicsQualityMode.Level6, 6);
        map.put(GraphicsQualityMode.Level7, 7);
        map.put(GraphicsQualityMode.Level8, 8);
        map.put(GraphicsQualityMode.Level9, 9);
        map.put(GraphicsQualityMode.Level10, 10);
        GraphicsQualityModes = Collections.unmodifiableMap(map);
    }

    public static final Map<VolumeMode, Integer> VolumeModes;
    static {
        Map<VolumeMode, Integer> map = new EnumMap<>(VolumeMode.class);
        map.put(VolumeMode.Level1, 1);
        map.put(VolumeMode.Level2, 2);
        map.put(VolumeMode.Level3, 3);
        map.put(VolumeMode.Level4, 4);
        map.put(VolumeMode.Level5, 5);
        map.put(VolumeMode.Level6, 6);
        map.put(VolumeMode.Level7, 7);
        map.put(VolumeMode.Level8, 8);
        map.put(VolumeMode.Level9, 9);
        map.put(VolumeMode.Level10, 10);
        VolumeModes = Collections.unmodifiableMap(map);
    }

//    public static final Map<LanguageRecreated, String> LanguagesRecreated;
//    static {
//        Map<LanguageRecreated, String> map = new EnumMap<>(LanguageRecreated.class);
//        map.put(LanguageRecreated.Automatic, null);
//        map.put(LanguageRecreated.Arabic, "ar");
//        map.put(LanguageRecreated.Filipino, "fil");
//        map.put(LanguageRecreated.Hindi, "hi");
//        map.put(LanguageRecreated.Indonesian, "id");
//        map.put(LanguageRecreated.Korean, "ko");
//        map.put(LanguageRecreated.Malay, "ms");
//        map.put(LanguageRecreated.Portuguese, "pt");
//        map.put(LanguageRecreated.Portuguese_Brazilian, "pt-br");
//        map.put(LanguageRecreated.Russian, "ru");
//        map.put(LanguageRecreated.Spanish, "es");
//        map.put(LanguageRecreated.Thai, "th");
//        map.put(LanguageRecreated.Vietnamese, "vi");
//
//        LanguagesRecreateds = Collections.unmodifiableMap(map);
//    }

    public static final Map<ThemeRecreated, String> ThemeRecreateds;
    static {
        Map<ThemeRecreated, String> map = new EnumMap<>(ThemeRecreated.class);
        map.put(ThemeRecreated.dark, "dark");
        map.put(ThemeRecreated.light, "light");
        ThemeRecreateds = Collections.unmodifiableMap(map);
    }

    public static final Map<RobloxAppType, String> RobloxAppTypes;
    static {
        Map<RobloxAppType, String> map = new EnumMap<>(RobloxAppType.class);
        map.put(RobloxAppType.global, "global");
        map.put(RobloxAppType.vng, "vng");
        RobloxAppTypes = Collections.unmodifiableMap(map);
    }

    @Override
    public String getClassName() {
        return "ConfigManager";
    }

    @Override
    public File getFileLocation() {
        File dir = new File(Paths.getLocalAppData());
        if (!dir.exists()) {
            boolean success = dir.mkdirs();
            if (!success) {
                Log.w("Config", "Could not create directory: " + dir.getAbsolutePath());
            }
        }
        return new File(dir, "Config.json");
    }

    public void setSettingValue(String key, Object value) {
        final String LOG_IDENT = "AppSettings::SetValue";

        try {
            if (value == null) {
                if (prop.containsKey(key)) {
                    App.getLogger().writeLine(LOG_IDENT, "Deletion of " + key + " is pending");
                    prop.remove(key);
                }
            } else {
                if (prop.containsKey(key)) {
                    App.getLogger().writeLine(LOG_IDENT, "Changing of " + key + " from " + prop.get(key) + " to " + value + " is pending");
                } else {
                    App.getLogger().writeLine(LOG_IDENT, "Setting of " + key + " to " + value + " is pending");
                }
                prop.put(key, value);
            }
        } catch (Exception e) {
            App.getLogger().writeLine(LOG_IDENT, "Error processing key " + key);
            App.getLogger().writeException(LOG_IDENT, e);
        }
    }

    @Override
    public void save() {
        final String LOG = "ConfigManager::save";
        ChevstrapSettings settings = App.getChevstrapSettings();

        settings.asMap().forEach((key, val) -> {
            try { setSettingValue(key, val); }
            catch (Exception e) {
                App.getLogger().writeLine(LOG, "Error saving key: " + key);
                App.getLogger().writeException(LOG, e);
            }
        });

        Map<String, Object> stringified = new LinkedHashMap<>();
        prop.forEach((key, val) -> {
            if (!"fflags".equals(key)) {
                stringified.put(key, val);
            } else {
                Map<String, Object> converted = new LinkedHashMap<>();
                if (val instanceof Map) {
                    for (Map.Entry<?, ?> entry : ((Map<?, ?>) val).entrySet()) {
                        Object rawValue = entry.getValue();
                        Object v;

                        if (rawValue instanceof Boolean) {
                            v = ((Boolean) rawValue) ? "True" : "False";
                        } else if (rawValue != null) {
                            v = String.valueOf(rawValue);
                        } else {
                            v = null;
                        }
                        converted.put((String) entry.getKey(), v);
                    }
                }
                stringified.put("fflags", converted);
            }
        });
        prop = stringified;
        super.save();

        originalProp = new LinkedHashMap<>(prop);
    }

    @Override
    public void load(boolean alertFailure) {
        super.load(alertFailure);

        ChevstrapSettings settings = App.getChevstrapSettings();
        prop.forEach((key, val) -> {
            if (!key.contains("fflags")) settings.setFromMap(key, val);
        });
        originalProp = new LinkedHashMap<>(prop);
    }

    @Override
    protected Map<String, Object> createEmptyMap() {
        return new LinkedHashMap<>();
    }
}