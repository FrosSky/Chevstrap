package com.chevstrap.rbx.AppDirectories;

import com.chevstrap.rbx.App;

public class RobloxClientData extends CommonAppData {
    public RobloxClientData() {}

    @Override
    public String getExecutablePackage() {
        String preferred = App.getChevstrapSettings().getPreferredRobloxApp();
        if ("vng".equals(preferred)) {
            return "com.roblox.client.vnggames";
        } else if ("global".equals(preferred)) {
            return "com.roblox.client";
        } else if ("custom".equals(preferred)) {
                return App.getChevstrapSettings().getCustomRobloxClientPackage();
        } else {
            return "com.roblox.client";
        }
    }
}