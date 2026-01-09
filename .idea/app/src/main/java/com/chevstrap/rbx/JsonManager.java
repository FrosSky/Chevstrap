package com.chevstrap.rbx;

import com.chevstrap.rbx.Utility.FileTool;
import com.chevstrap.rbx.Utility.MD5Hash;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
        import java.util.*;

public abstract class JsonManager<T extends Map<String, Object>> {
    protected T prop;
    protected T originalProp;
    protected boolean loaded = false;
    private String lastFileHash = null;

    public abstract String getClassName();
    public abstract File getFileLocation();
    protected abstract T createEmptyMap();

    public boolean isLoaded() {
        return loaded;
    }

    public void save() {
        File file = getFileLocation();
        JSONObject json = new JSONObject(prop);
        try {
            FileTool.safeWrite(file, json.toString(4));
            lastFileHash = MD5Hash.fromFile(file.getAbsolutePath());
        } catch (IOException | JSONException e) {
            throw new RuntimeException("Failed to save " + getClassName() + ": " + e.getMessage(), e);
        }
    }

    public void load(boolean alertFailure) {
        String LOG_IDENT = "JsonManager::Load";
        File file = getFileLocation();

        if (!file.exists()) {
            prop = createEmptyMap();
            loaded = false;
            lastFileHash = null;
            return;
        }

        try {
            String currentHash = MD5Hash.fromFile(file.getAbsolutePath());

            if (!hasFileChanged() && loaded) {
                return;
            }

            if (currentHash != null && currentHash.equals(lastFileHash)) {
                loaded = true;
                return;
            }

            String jsonData = FileTool.read(file);
            JSONObject jsonObject = new JSONObject(jsonData);

            prop = convertJsonObjectToMap(jsonObject);

            lastFileHash = currentHash;
            loaded = true;

        } catch (IOException | JSONException e) {
            if (alertFailure) {
                App.getLogger().writeLine(LOG_IDENT, "Failed to load " + getClassName() + ": " + e.getMessage());
            }

            try {
                if (file.exists()) {
                    String backupName = getClassName() + "_backup.json";
                    File backupFile = new File(file.getParentFile(), backupName);
                    FileTool.copy(file, backupFile, true);

                    App.getLogger().writeLine(LOG_IDENT, "Backup created: " + backupFile.getAbsolutePath());
                }
            } catch (Exception copyEx) {
                App.getLogger().writeLine(LOG_IDENT, "Failed to create backup: " + copyEx.getMessage());
            }

            prop = createEmptyMap();
            loaded = false;
            lastFileHash = null;
            save();
        }
    }

    private T convertJsonObjectToMap(JSONObject jsonObject) throws JSONException {
        T map = createEmptyMap();
        JSONArray keys = jsonObject.names();
        if (keys == null) return map;

        for (int i = 0; i < keys.length(); i++) {
            String key = keys.getString(i);
            Object value = jsonObject.get(key);

            if (value instanceof JSONObject) {
                value = convertJsonObjectToMap((JSONObject) value);
            } else if (value instanceof JSONArray) {
                value = convertJsonArrayToList((JSONArray) value);
            }
            map.put(key, value);
        }
        return map;
    }

    private List<Object> convertJsonArrayToList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);

            if (value instanceof JSONObject) {
                value = convertJsonObjectToMap((JSONObject) value);
            } else if (value instanceof JSONArray) {
                value = convertJsonArrayToList((JSONArray) value);
            }
            list.add(value);
        }

        return list;
    }

    public String getCurrentFileHash() {
        File file = getFileLocation();
        if (file.exists()) {
            return MD5Hash.fromFile(file.getAbsolutePath());
        }
        return null;
    }

    public boolean hasFileChanged() {
        String currentHash = getCurrentFileHash();
        return currentHash != null && !currentHash.equals(lastFileHash);
    }
}
