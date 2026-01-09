package com.chevstrap.rbx;

import com.chevstrap.rbx.Utility.FileTool;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class Installer {
    public void HandleUpgrades() {
        String currentVersion = App.getCurrentVersion(App.getAppContext());
        String targetVersion = "1.9";
        if (currentVersion == null) return;

        Utilities.VersionComparison result = Utilities.compareVersions(targetVersion, currentVersion);

        if (result == Utilities.VersionComparison.LESS) {
            File ClientAppSettingsOld = new File(Paths.getModifications(), "ClientSettings/ClientAppSettings.json");
            if (FileTool.isExist(ClientAppSettingsOld.toString())) {
                try  {
                    FileTool.deleteFile(ClientAppSettingsOld);
                } catch (Exception ignored) {
                }
            }
            File LastClientAppSettingsOld = new File(Paths.getModifications(), "ClientSettings/LastClientAppSettings.json");
            if (FileTool.isExist(LastClientAppSettingsOld.toString())) {
                try  {
                    FileTool.deleteFile(LastClientAppSettingsOld);
                } catch (Exception ignored) {}
            }
            FileTool.deleteDir(new File(Paths.getModifications()), true);
        }
    }
}