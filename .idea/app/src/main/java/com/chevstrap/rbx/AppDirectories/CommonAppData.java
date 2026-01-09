package com.chevstrap.rbx.AppDirectories;

import android.content.pm.PackageManager;
import com.chevstrap.rbx.App;

import java.io.File;

public abstract class CommonAppData {
    public abstract String getExecutablePackage();

    public CommonAppData() {}

    public String getExecutablePath() {
        try {
            String robloxDir = getRobloxDirectory();
            if (robloxDir != null) {
                return new File(robloxDir, "appData").getAbsolutePath();
            }
        } catch (Exception e) {
            App.getLogger().writeException("CommonAppData:getExecutablePath", e);
        }
        return null;
    }

    public String getRobloxDirectory() {
        String packageName = getExecutablePackage();
        if (packageName == null) return null;

        try {
            String dataDir = App.getAppContext()
                    .getPackageManager()
                    .getApplicationInfo(packageName, 0)
                    .dataDir;

            if (dataDir != null) {
                return new File(dataDir, "files").getAbsolutePath();
            } else {
                App.getLogger().writeLine("CommonAppData:getRobloxDirectory", "dataDir is null");
            }
        } catch (PackageManager.NameNotFoundException e) {
            App.getLogger().writeLine("CommonAppData:getRobloxDirectory", "failed getting package");
            App.getLogger().writeException("CommonAppData:getRobloxDirectory", e);
        }
        String fallback = App.getAppContext().getFilesDir().getAbsolutePath()
                .replace("/" + App.getAppContext().getPackageName() + "/", "/" + packageName + "/");
        return fallback + "/";
    }
}
