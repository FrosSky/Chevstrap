package com.chevstrap.rbx.UI.ViewModels.Settings;

import com.chevstrap.rbx.App;

public class IntegrationsViewModel {
    public IntegrationsViewModel() {

    }

//    public int getFramerateLimit() {
//        try {
//            if (manager.getSettingValue("set_in_game_framerate_limit") == null) {
//                return 0;
//            } else {
//                return (int) manager.getSettingValue("set_in_game_framerate_limit");
//            }
//        } catch (Exception e) {
//            return 0;
//        }
//    }
//
//    public void setFramerateLimit(String val) {
//        int value = Integer.parseInt(val);
//        boolean remove_it = val.isEmpty() || value <= 0;
//
//        if (remove_it) {
//            manager.setSettingValue("set_in_game_framerate_limit", null);
//        } else {
//            if (Integer.parseInt(val) == 0) {
//                manager.setSettingValue("set_in_game_framerate_limit", -1);
//            } else {
//                manager.setSettingValue("set_in_game_framerate_limit", value);
//            }
//        }
//
//        if (value > 240) {
//            Frontend.ShowMessageBox(App.getSavedFragmentActivity(), App.getTextLocale(App.getAppContext(), R.string.menu_integrations_setframeratelimit_240_warning));
//        }
//    }

    public boolean isQueryServerLocation() {
        return App.getChevstrapSettings().isServerLocationIndicatorEnabled();
    }

    public void setQueryServerLocation(boolean value) {
        App.getChevstrapSettings().setServerLocationIndicatorEnabled(value);
    }

    public boolean isActivityTrackerEnabled() {
        return App.getChevstrapSettings().isActivityTrackerEnabled();
    }

    public void setActivityTrackerEnabled(boolean value) {
        App.getChevstrapSettings().setActivityTrackerEnabled(value);
    }
}