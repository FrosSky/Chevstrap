package com.chevstrap.rbx.UI.Elements.Settings.Pages;

import android.content.Context;
import android.widget.LinearLayout;

import com.chevstrap.rbx.App;
import com.chevstrap.rbx.Enums.RobloxAppType;
import com.chevstrap.rbx.Enums.ThemeRecreated;
import com.chevstrap.rbx.R;
import com.chevstrap.rbx.UI.ViewModels.Settings.BehaviourViewModel;
import com.chevstrap.rbx.UI.ViewModels.Settings.ChevstrapViewModel;
import com.chevstrap.rbx.Models.MethodPair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;

public class Chevstrap {

    public static void addEveryPresets(Context context, LinearLayout parentLayout, ChevstrapFragment fragment) {
        fragment.addSection(parentLayout, getTextLocale(context, R.string.common_general));

        fragment.addAccordionMenu(getTextLocale(context, R.string.menu_settings_accordion_title_appearence), getTextLocale(context, R.string.menu_settings_accordion_description_appearence), () -> addA(context, fragment), 1);
//        try {
//            JSONArray array = new JSONArray();
//
//            for (LanguageRecreateds mode : LanguageRecreateds.values()) {
//                JSONObject obj = new JSONObject()
//                        .put("label", mode.getDisplayName())
//                        .put("value", mode.name());
//                array.put(obj);
//            }
//
//            Method getMethod = SettingsViewModel.class.getMethod("getAppLanguage");
//            Method setMethod = SettingsViewModel.class.getMethod("setAppLanguage", String.class);
//
//            MethodPair methods = new MethodPair(getMethod, setMethod);
//            fragment.addDropdown(getTextLocale(context, R.string.menu_settings_language_title), getTextLocale(context, R.string.menu_settings_language_description), parentLayout, array, methods);
//        } catch (JSONException ignored) {
//        } catch (NoSuchMethodException e) {
//            throw new RuntimeException(e);
//        }
    }

    public static void addA(Context context, ChevstrapFragment fragment) {
        try {
            JSONArray array = new JSONArray();

            for (ThemeRecreated mode : ThemeRecreated.values()) {
                JSONObject obj = new JSONObject()
                        .put("label", mode.getDisplayName())
                        .put("value", mode.name());
                array.put(obj);
            }

            Method getMethod = ChevstrapViewModel.class.getMethod("getapp_theme_in_app");
            Method setMethod = ChevstrapViewModel.class.getMethod("setapp_theme_in_app", String.class);

            MethodPair methods = new MethodPair(getMethod, setMethod);
            fragment.addDropdown(getTextLocale(context, R.string.menu_settings_color_theme_in_app_title), getTextLocale(context, R.string.menu_settings_color_theme_in_app_description), array, methods, 1);
        } catch (JSONException e) {
//            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        fragment.addImportButton(getTextLocale(App.getAppContext(), R.string.menu_settings_background_video_title), getTextLocale(App.getAppContext(), R.string.menu_settings_background_video_description), null, "set_animated_theme",  1);

        try {
            Method getMethod = ChevstrapViewModel.class.getMethod("isForcePotraitMode");
            Method setMethod = ChevstrapViewModel.class.getMethod("setForcePotraitMode", boolean.class);
            MethodPair methods = new MethodPair(getMethod, setMethod);
            fragment.addToggle(
                    getTextLocale(context, R.string.menu_settings_force_portrait_mode_title),
                    getTextLocale(context, R.string.menu_settings_force_portrait_mode_description),
                    methods, 1
            );
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getTextLocale(Context context, int resId) {
        return context.getString(resId);
    }
}