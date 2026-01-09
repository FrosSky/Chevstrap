package com.chevstrap.rbx.UI.Elements.Settings.Pages;

import android.content.Context;
import android.widget.LinearLayout;

import com.chevstrap.rbx.R;
import com.chevstrap.rbx.UI.ViewModels.Settings.ChevstrapViewModel;
import com.chevstrap.rbx.Models.MethodPair;
import com.chevstrap.rbx.UI.ViewModels.Settings.LocalPreferencesViewModel;

import java.lang.reflect.Method;

public class LocalPreferences {

    public static void addEveryPresets(Context context, LocalPreferencesFragment fragment) {
        fragment.addButton(
                getTextLocale(context, R.string.menu_devicepreferences_help_title),
                getTextLocale(context, R.string.menu_devicepreferences_help_description),
                "https://github.com/FrosSky/Chevstrap/wiki/Device-Preferences-Guide-For-Android",
                "link", -1
        );

        try {
            Method getMethod = LocalPreferencesViewModel.class.getMethod("isUseDevicePrefs");
            Method setMethod = LocalPreferencesViewModel.class.getMethod("setUseDevicePrefs", boolean.class);
            fragment.addToggle(
                    getTextLocale(context, R.string.menu_devicepreferences_allowmanagelocalpreferences_title),
                    getTextLocale(context, R.string.menu_devicepreferences_allowmanagelocalpreferences_description),
                    new MethodPair(getMethod, setMethod),
                    -1
            );
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        fragment.addSection(getTextLocale(context, R.string.common_general));

        try {
            Method getMethod = LocalPreferencesViewModel.class.getMethod("isReducedMotion");
            Method setMethod = LocalPreferencesViewModel.class.getMethod("setReducedMotion", boolean.class);
            MethodPair methods = new MethodPair(getMethod, setMethod);
            fragment.addToggle(
                    getTextLocale(context, R.string.menu_devicepreferences_reducemotion_title),
                    getTextLocale(context, R.string.menu_devicepreferences_reducemotion_description),
                    methods,
                    -1
            );
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        try {
            Method getMethod = LocalPreferencesViewModel.class.getMethod("isInGameHaptics");
            Method setMethod = LocalPreferencesViewModel.class.getMethod("setInGameHaptics", boolean.class);
            MethodPair methods = new MethodPair(getMethod, setMethod);
            fragment.addToggle(
                    getTextLocale(context, R.string.menu_devicepreferences_haptics_title),
                    getTextLocale(context, R.string.menu_devicepreferences_haptics_description),
                    methods,
                    -1
            );
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getTextLocale(Context context, int resId) {
        return context.getString(resId);
    }
}