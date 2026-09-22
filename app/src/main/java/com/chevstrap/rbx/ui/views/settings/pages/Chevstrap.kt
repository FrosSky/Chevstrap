package com.chevstrap.rbx.ui.views.settings.pages

import android.content.Context
import android.widget.LinearLayout
import com.chevstrap.rbx.App
import com.chevstrap.rbx.R
import com.chevstrap.rbx.enums.ThemeRecreated
import com.chevstrap.rbx.extensions.ResourceManagerEx
import com.chevstrap.rbx.ui.viewModels.MethodAction
import com.chevstrap.rbx.ui.viewModels.MethodPair
import com.chevstrap.rbx.ui.viewModels.settings.pages.ChevstrapViewModel
import org.json.JSONArray
import org.json.JSONObject

object Chevstrap {

    private val viewModel = ChevstrapViewModel()

    fun addEveryPresets(
        context: Context,
        parentLayout: LinearLayout,
        fragment: ChevstrapFragment
    ) {

        fragment.addSection(
            parentLayout,
            ResourceManagerEx.getStringOrEmpty(
                context,
                R.string.common_general
            )
        )

        fragment.addAccordionMenu(
            ResourceManagerEx.getStringOrEmpty(
                context,
                R.string.menu_settings_accordion_title_appearence
            ),
            ResourceManagerEx.getStringOrEmpty(
                context,
                R.string.menu_settings_accordion_description_appearence
            ),
            {
                addA(
                    context,
                    fragment
                )
            },
            1
        )

        fragment.addButton(
            ResourceManagerEx.getStringOrEmpty(
                App.appContext!!,
                R.string.menu_settings_open_logs_viewer_title
            ),
            ResourceManagerEx.getStringOrEmpty(
                App.appContext!!,
                R.string.menu_settings_open_logs_viewer_description
            ),
            MethodAction { viewModel.logExplorer() },
            -1
        )
    }

    private fun addA(
        context: Context,
        fragment: ChevstrapFragment
    ) {

        val array = JSONArray()
        for (mode in ThemeRecreated.entries) {
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
            ResourceManagerEx.getStringOrEmpty(
                context,
                R.string.menu_settings_app_theme_in_app_title
            ),
            "",
            array,
            MethodPair(
                viewModel,
                ChevstrapViewModel::appThemeInApp
            ),
            1
        )

        fragment.addButton(
            ResourceManagerEx.getStringOrEmpty(
                App.appContext!!,
                R.string.menu_settings_background_image_title
            ),
            ResourceManagerEx.getStringOrEmpty(
                App.appContext!!,
                R.string.menu_settings_background_image_description
            ),
            MethodAction { viewModel.importCustomAppBackground() },
            1
        )
    }
}