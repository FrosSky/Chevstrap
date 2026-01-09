package com.chevstrap.rbx;

import com.chevstrap.rbx.Integrations.ActivityWatcher;
import com.chevstrap.rbx.Models.Entities.ActivityData;

import java.util.Collection;

public class CustomWatcher {
    private static final String LOG_IDENT = "CustomWatcher";
    private static CustomWatcher instance;
    private ActivityWatcher activityWatcher;
    private ActivityData activityData;

    public static synchronized CustomWatcher getInstance() {
        if (instance == null) {
            instance = new CustomWatcher();
        } else {
            App.getLogger().writeLine(LOG_IDENT, "CustomWatcher instance already exists");
        }
        return instance;
    }

    private CustomWatcher() {
        try {
            activityWatcher = new ActivityWatcher();
        } catch (Exception e) {
            App.getLogger().writeException(LOG_IDENT, e);
        }
    }

    public void setActivityData(ActivityData data) {
        this.activityData = data;
    }

    public ActivityData getActivityData() {
        return activityData;
    }

    public void run() {
        if (activityWatcher != null) {
            activityWatcher.runWatcher();
        }
    }

    public Collection<ActivityData> getAllHistoryServer() {
        if (activityWatcher != null) {
            return activityWatcher.history;
        }
        return null;
    }

    public void Dispose() {
        App.getLogger().writeLine(LOG_IDENT, "Disposing CustomWatcher");

        if (activityWatcher != null && !activityWatcher.isStopMonitoring()) {
            activityWatcher.Dispose();
        }

        activityWatcher = null;
        activityData = null;
        instance = null;
    }
}
