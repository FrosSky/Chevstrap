package com.chevstrap.rbx.ui.views.settings.pages

import android.content.Context
import android.widget.LinearLayout
import com.chevstrap.rbx.App
import com.chevstrap.rbx.R
import com.chevstrap.rbx.extensions.ResourceManagerEx
import com.chevstrap.rbx.ui.viewModels.MethodAction
import com.chevstrap.rbx.ui.viewModels.settings.pages.AboutViewModel

object AboutOne {

    private val viewModel = AboutViewModel()

    fun addEveryPresets(
        context: Context,
        parentLayout: LinearLayout?,
        fragment: AboutOneFragment
    ) {
        fragment.addAccordionMenu(
            "Chevstrap",
            "",
            { addA(fragment, parentLayout) },
            1,
            parentLayout
        )

        fragment.addSection(
            parentLayout,
            ResourceManagerEx.getStringOrEmpty(
                context,
                R.string.about_section_contributors_title
            )
        )

        fragment.addAccordionMenu(
            ResourceManagerEx.getStringOrEmpty(
                context,
                R.string.about_contributors_code_title
            ),
            "",
            { addC(fragment, parentLayout) },
            2,
            parentLayout
        )

        fragment.addAccordionMenu(
            ResourceManagerEx.getStringOrEmpty(
                context,
                R.string.about_section_special_thanks_title
            ),
            "",
            { addS(fragment, parentLayout) },
            4,
            parentLayout
        )
    }

    fun addA(
        uiHelpers: AboutOneFragment,
        parentLayout: LinearLayout?
    ) {
        uiHelpers.addButton(
            ResourceManagerEx.getStringOrEmpty(
                App.appContext,
                R.string.about_github_repository_title
            ),
            "",
            parentLayout,
            MethodAction {
                viewModel.projectRepositoryPage()
            },
            1
        )

        uiHelpers.addButton(
            ResourceManagerEx.getStringOrEmpty(
                App.appContext,
                R.string.about_help_and_information
            ),
            "",
            parentLayout,
            MethodAction {
                viewModel.projectWikiPage()
            },
            1
        )

        uiHelpers.addButton(
            ResourceManagerEx.getStringOrEmpty(
                App.appContext,
                R.string.common_discord_server
            ),
            "",
            parentLayout,
            MethodAction {
                viewModel.discordServerPage()
            },
            1
        )
    }

    fun addC(
        uiHelpers: AboutOneFragment,
        parentLayout: LinearLayout?
    ) {
        uiHelpers.addButton(
            "FrosSky",
            "",
            parentLayout,
            MethodAction {
                viewModel.fsPage()
            },
            2
        )
    }

    fun addS(
        uiHelpers: AboutOneFragment,
        parentLayout: LinearLayout?
    ) {
        uiHelpers.addButton(
            "Bloxstrap",
            ResourceManagerEx.getStringOrEmpty(
                App.appContext,
                R.string.about_special_thanks_bloxstrap_contributors_desc_title
            ),
            parentLayout,
            MethodAction {
                viewModel.bsPage()
            },
            4
        )
    }

}