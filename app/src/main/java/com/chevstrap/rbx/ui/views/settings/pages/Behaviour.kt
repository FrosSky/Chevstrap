package com.chevstrap.rbx.ui.views.settings.pages

import android.content.Context
import android.widget.LinearLayout
import com.chevstrap.rbx.R
import com.chevstrap.rbx.enums.LaunchMode
import com.chevstrap.rbx.extensions.ResourceManagerEx
import com.chevstrap.rbx.ui.viewModels.MethodPair
import com.chevstrap.rbx.ui.viewModels.settings.pages.BehaviourViewModel
import org.json.JSONArray
import org.json.JSONObject

object Behaviour {

    private val viewModel = BehaviourViewModel()

    fun addEveryPresets(
        context: Context,
        parentLayout: LinearLayout?,
        fragment: BehaviourFragment
    ) {

        val array = JSONArray()

        for (mode in LaunchMode.entries) {
            array.put(
                JSONObject()
                    .put("label", mode.displayName)
                    .put("value", mode.name)
            )
        }


        fragment.addDropdown(
            ResourceManagerEx.getStringOrEmpty(
                context,
                R.string.menu_behaviour_preferred_roblox_app_title
            ),
            ResourceManagerEx.getStringOrEmpty(
                context,
                R.string.menu_behaviour_preferred_roblox_app_description
            ),
            parentLayout,
            array,
            MethodPair(
                viewModel,
                BehaviourViewModel::preferredRobloxApp
            )
        )


        fragment.addToggle(
            ResourceManagerEx.getStringOrEmpty(
                context,
                R.string.menu_behaviour_check_latest_release_title
            ),
            ResourceManagerEx.getStringOrEmpty(
                context,
                R.string.menu_behaviour_check_latest_release_description
            ),
            parentLayout,
            MethodPair(
                viewModel,
                BehaviourViewModel::bringToLatestUpdate
            )
        )
    }
}