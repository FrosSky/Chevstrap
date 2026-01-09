package com.chevstrap.rbx.UI.Elements.Settings.Pages;

import android.content.Context;
import android.widget.LinearLayout;

import com.chevstrap.rbx.App;
import com.chevstrap.rbx.R;

public class AboutOne {
    public static void addEveryPresets(Context context, LinearLayout parentLayout, AboutOneFragment fragment) {
        fragment.addAccordionMenu(getTextLocale(context, R.string.common_chevstrap), "", () -> addA(fragment, parentLayout),1, parentLayout);

        fragment.addSection(parentLayout, getTextLocale(context, R.string.about_section_contributors_title));
        fragment.addAccordionMenu(getTextLocale(context, R.string.about_code_and_uidesign_title), "", () -> addC(fragment, parentLayout),2, parentLayout);
        fragment.addAccordionMenu(getTextLocale(context, R.string.about_proofreader_title), "", () -> addP(fragment, parentLayout),3, parentLayout);
        fragment.addAccordionMenu(getTextLocale(context, R.string.about_section_special_thanks_title), "", () -> addS(fragment, parentLayout),4, parentLayout);
    }

    public static void addA(AboutOneFragment uiHelpers, LinearLayout parentLayout) {
        uiHelpers.addButton(App.getTextLocale(App.getAppContext(), R.string.about_github_repository_title), "", parentLayout, "https://github.com/" + App.ProjectRepository, "link", 1);
        uiHelpers.addButton(App.getTextLocale(App.getAppContext(), R.string.about_help_and_information), "", parentLayout, App.ProjectHelpLink, "link", 1);
        uiHelpers.addButton(App.getTextLocale(App.getAppContext(), R.string.about_report_an_issue_or_feature_request_title), "", parentLayout, App.ProjectSupportLink, "link", 1);
    }

    public static void addC(AboutOneFragment uiHelpers, LinearLayout parentLayout) {
        uiHelpers.addButton("FrosSky", getTextLocale(App.getAppContext(), R.string.about_code_and_uidesign_title), parentLayout, "https://github.com/FrosSky", "link", 2);
    }

    public static void addS(AboutOneFragment uiHelpers, LinearLayout parentLayout) {
        uiHelpers.addButton(getTextLocale(App.getAppContext(), R.string.about_bloxstrap_contributors), getTextLocale(App.getAppContext(), R.string.about_bloxstrap_contributors_desc_title), parentLayout, "https://github.com/bloxstraplabs/bloxstrap/graphs/contributors", "link", 4);
//        uiHelpers.addButton(getTextLocale(App.getAppContext(), R.string.about_roblox_client_tracker_contributors), getTextLocale(App.getAppContext(), R.string.about_roblox_client_tracker_contributors_desc_title), parentLayout, "https://github.com/MaximumADHD/Roblox-Client-Tracker/graphs/contributors", "link", 4);
    }

    public static void addP(AboutOneFragment uiHelpers, LinearLayout parentLayout) {
        uiHelpers.addButton("LeventGameing", getTextLocale(App.getAppContext(), R.string.about_proofreader_title), parentLayout, "https://github.com/LeventGameing", "link", 3);
    }


    private static String getTextLocale(Context context, int resId) {
        return context.getString(resId);
    }
}