package com.chevstrap.rbx.ui.views.settings.pages

import android.content.Context
import android.widget.LinearLayout
import com.chevstrap.rbx.App
import chevstrap.preference.PrefsManager
import com.chevstrap.rbx.R
import com.chevstrap.rbx.enums.DiscordStatusType
import com.chevstrap.rbx.extensions.ResourceManagerEx
import com.chevstrap.rbx.ui.viewModels.MethodAction
import com.chevstrap.rbx.ui.viewModels.MethodPair
import com.chevstrap.rbx.ui.viewModels.settings.pages.IntegrationsViewModel
import org.json.JSONArray
import org.json.JSONObject

object Integrations {

    private val viewModel = IntegrationsViewModel()

    fun addEveryPresets(
        context: Context,
        parentLayout: LinearLayout,
        fragment: IntegrationsFragment
    ) {

        fragment.addSection(
            parentLayout,
            ResourceManagerEx.getStringOrEmpty(
                context,
                R.string.menu_integrations_section_activity_tracking
            )
        )

        fragment.addToggle(
            "enable_activity_tracking",
            ResourceManagerEx.getStringOrEmpty(
                context,
                R.string.menu_integrations_enable_activity_tracking_title
            ),
            ResourceManagerEx.getStringOrEmpty(
                context,
                R.string.menu_integrations_enable_activity_tracking_description
            ),
            parentLayout,
            MethodPair(
                viewModel,
                IntegrationsViewModel::activityTrackerEnabled
            ),
            null
        )

        fragment.addToggle(
            "query_server_location",
            ResourceManagerEx.getStringOrEmpty(
                context,
                R.string.menu_integrations_query_server_location_title
            ),
            ResourceManagerEx.getStringOrEmpty(
                context,
                R.string.menu_integrations_query_server_location_description
            ),
            parentLayout,
            MethodPair(
                viewModel,
                IntegrationsViewModel::queryServerLocation
            ),
            "enable_activity_tracking"
        )

        fragment.addButton(
            ResourceManagerEx.getStringOrEmpty(
                App.appContext!!,
                R.string.menu_integrations_game_history_title
            ),
            ResourceManagerEx.getStringOrEmpty(
                App.appContext!!,
                R.string.menu_integrations_game_history_description
            ),
            parentLayout,
            MethodAction { viewModel.serverHistoryDialog() },
        )

        fragment.addSection(
            parentLayout,
            ResourceManagerEx.getStringOrEmpty(
                context,
                R.string.menu_integrations_section_discord_gateway
            )
        )

        fragment.addButton(
            if (PrefsManager.getToken().isEmpty()) {
                ResourceManagerEx.getStringOrEmpty(
                    context,
                    R.string.menu_integrations_login_with_discord_title
                )
            } else {
                ResourceManagerEx.getStringOrEmpty(
                    context,
                    R.string.menu_integrations_discord_account_title
                )
            },
            "",
            parentLayout,
            MethodAction { viewModel.myDiscordAccountPage() },
        )

        val array = JSONArray()
        for (mode in DiscordStatusType.entries) {

            array.put(
                JSONObject()
                    .put(
                        "label",
                        mode.displayName
                    )
                    .put(
                        "value",
                        mode.name
                    )
            )
        }

        fragment.addDropdown(
            "discord_set_status",
            ResourceManagerEx.getStringOrEmpty(
                context,
                R.string.menu_integrations_discord_status_title
            ),
            ResourceManagerEx.getStringOrEmpty(
                context,
                R.string.menu_integrations_discord_status_description
            ),
            parentLayout,
            array,
            MethodPair(
                viewModel,
                IntegrationsViewModel::discordSetOnlineStatus
            ),
            null
        )

        fragment.addSection(
            parentLayout,
            ResourceManagerEx.getStringOrEmpty(
                context,
                R.string.menu_integrations_section_discord_rich_presence
            )
        )

        fragment.addToggle(
            "discord_show_game_activity",
            ResourceManagerEx.getStringOrEmpty(
                context,
                R.string.menu_integrations_show_game_activity_title
            ),
            ResourceManagerEx.getStringOrEmpty(
                context,
                R.string.menu_integrations_show_game_activity_description
            ),
            parentLayout,
            MethodPair(
                viewModel,
                IntegrationsViewModel::showGameActivity
            ),
            "enable_activity_tracking"
        )

        fragment.addToggle(
            "allow_activity_joining",
            ResourceManagerEx.getStringOrEmpty(
                context,
                R.string.menu_integrations_allow_activity_joining_title
            ),
            ResourceManagerEx.getStringOrEmpty(
                context,
                R.string.menu_integrations_allow_activity_joining_description
            ),
            parentLayout,
            MethodPair(
                viewModel,
                IntegrationsViewModel::allowActivityJoining
            ),
            "discord_show_game_activity"
        )

        fragment.addToggle(
            "show_roblox_account",
            ResourceManagerEx.getStringOrEmpty(
                context,
                R.string.menu_integrations_show_roblox_account_title
            ),
            ResourceManagerEx.getStringOrEmpty(
                context,
                R.string.menu_integrations_show_game_activity_description
            ),
            parentLayout,
            MethodPair(
                viewModel,
                IntegrationsViewModel::showRobloxAccount
            ),
            "discord_show_game_activity"
        )

        fragment.addToggle(
            "discord_allow_custom_presence",
            ResourceManagerEx.getStringOrEmpty(
                context,
                R.string.menu_integrations_allow_custom_presence_title
            ),
            ResourceManagerEx.getStringOrEmpty(
                context,
                R.string.menu_integrations_allow_custom_presence_description
            ),
            parentLayout,
            MethodPair(
                viewModel,
                IntegrationsViewModel::discordAllowCustomPresence
            ),
            "discord_show_game_activity"
        )

        fragment.addToggle(
            "discord_verified_custom_presence_only",
            ResourceManagerEx.getStringOrEmpty(
                context,
                R.string.menu_integrations_verified_custom_presence_only_title
            ),
            ResourceManagerEx.getStringOrEmpty(
                context,
                R.string.menu_integrations_verified_custom_presence_only_description
            ),
            parentLayout,
            MethodPair(
                viewModel,
                IntegrationsViewModel::discordVerifiedCustomPresenceOnly
            ),
            "discord_allow_custom_presence"
        )
    }
}