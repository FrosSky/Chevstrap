package com.chevstrap.rbx.UI.Elements.Settings.Pages;

import android.content.Context;
import android.widget.LinearLayout;

import com.chevstrap.rbx.App;
import com.chevstrap.rbx.Models.MethodPair;
import com.chevstrap.rbx.R;
import com.chevstrap.rbx.UI.ViewModels.Settings.IntegrationsViewModel;

import java.lang.reflect.Method;

public class Integrations {

    public static void addEveryPresets(Context context, LinearLayout parentLayout, IntegrationsFragment fragment) {
        fragment.addSection(parentLayout, getTextLocale(context, R.string.menu_integrations_section_activitytracking));

        try {
            Method getMethod = IntegrationsViewModel.class.getMethod("isActivityTrackerEnabled");
            Method setMethod = IntegrationsViewModel.class.getMethod("setActivityTrackerEnabled", boolean.class);

            MethodPair methods = new MethodPair(getMethod, setMethod);
            fragment.addToggle("enable_activity_tracking",
                    getTextLocale(context, R.string.menu_integrations_enableactivitytracking_title),
                    getTextLocale(context, R.string.menu_integrations_enableactivitytracking_description), parentLayout, methods, null
            );
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        try {
            Method getMethod = IntegrationsViewModel.class.getMethod("isQueryServerLocation");
            Method setMethod = IntegrationsViewModel.class.getMethod("setQueryServerLocation", boolean.class);

            MethodPair methods = new MethodPair(getMethod, setMethod);
            fragment.addToggle("query_server_location",
                    getTextLocale(context, R.string.menu_integrations_queryserverlocation_title),
                    getTextLocale(context, R.string.menu_integrations_queryserverlocation_description),
                    parentLayout,
                    methods, "enable_activity_tracking");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        fragment.addImportButton(getTextLocale(App.getAppContext(), R.string.menu_integrations_experiencehistory_title), getTextLocale(App.getAppContext(), R.string.menu_integrations_experiencehistory_description), parentLayout, "open_server_history");
//        fragment.addSection(parentLayout, getTextLocale(context, R.string.menu_integrations_section_fpsunlocking));
//
//        try {
//            Method getMethod = IntegrationsViewModel.class.getMethod("getFramerateLimit");
//            Method setMethod = IntegrationsViewModel.class.getMethod("setFramerateLimit", String.class);
//
//            MethodPair methods = new MethodPair(getMethod, setMethod);
//            fragment.addTextbox(getTextLocale(context, R.string.menu_integrations_setframeratelimit_title), getTextLocale(context, R.string.menu_integrations_setframeratelimit_description), parentLayout, methods, "number");
//        } catch (NoSuchMethodException e) {
//            throw new RuntimeException(e);
//        }
    }

    private static String getTextLocale(Context context, int resId) {
        return context.getString(resId);
    }
}