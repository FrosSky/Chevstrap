package com.chevstrap.rbx.UI.Elements.Settings.Pages;

import android.content.Context;
import android.widget.LinearLayout;

import com.chevstrap.rbx.Enums.RobloxAppType;
import com.chevstrap.rbx.Models.MethodPair;
import com.chevstrap.rbx.R;
import com.chevstrap.rbx.UI.ViewModels.Settings.BehaviourViewModel;
import com.chevstrap.rbx.UI.ViewModels.Settings.ChevstrapViewModel;
import com.chevstrap.rbx.UI.ViewModels.Settings.IntegrationsViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;

public class Behaviour {

    public static void addEveryPresets(Context context, LinearLayout parentLayout, BehaviourFragment fragment) {
        try {
            JSONArray array = new JSONArray();

            for (RobloxAppType mode : RobloxAppType.values()) {
                JSONObject obj = new JSONObject()
                        .put("label", mode.getDisplayName())
                        .put("value", mode.name());
                array.put(obj);
            }

            Method getMethod = BehaviourViewModel.class.getMethod("get_preferred_roblox_app");
            Method setMethod = BehaviourViewModel.class.getMethod("set_preferred_roblox_app", String.class);

            MethodPair methods = new MethodPair(getMethod, setMethod);
            fragment.addDropdown(getTextLocale(context, R.string.menu_behaviour_preferred_roblox_app_title), getTextLocale(context, R.string.menu_behaviour_preferred_roblox_app_description), parentLayout, array, methods);
        } catch (JSONException e) {
//            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        try {
            Method getMethod = BehaviourViewModel.class.getMethod("isEnableBringToLatestUpdate");
            Method setMethod = BehaviourViewModel.class.getMethod("setEnableBringToLatestUpdate", boolean.class);
            MethodPair methods = new MethodPair(getMethod, setMethod);
            fragment.addToggle(
                    getTextLocale(context, R.string.menu_behaviour_bring_latest_release_title),
                    getTextLocale(context, R.string.menu_behaviour_bring_latest_release_description),
                    parentLayout,
                    methods
            );
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getTextLocale(Context context, int resId) {
        return context.getString(resId);
    }
}
