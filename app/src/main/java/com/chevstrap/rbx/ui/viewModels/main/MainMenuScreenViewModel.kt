package com.chevstrap.rbx.ui.viewModels.main

import com.chevstrap.rbx.App
import com.chevstrap.rbx.LaunchHandler
import com.chevstrap.rbx.LaunchSettings
import com.chevstrap.rbx.MainActivity
import com.chevstrap.rbx.ui.viewModels.GlobalViewModel

class MainMenuScreenViewModel {

    fun menuSettings() {
        try {
            val launchSettings = LaunchSettings(arrayOf("-menu"))
            val launchHandler = LaunchHandler(launchSettings)

            launchHandler.run()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun projectWikiPage() {
        try {
            GlobalViewModel.openWebpage(
                App.appContext,
                App.PROJECT_WIKI
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun launchRoblox() {
        try {
            val launchSettings = LaunchSettings(arrayOf("-client"))
            val launchHandler = LaunchHandler(launchSettings)

            val mainActivity = App.savedMainActivity
            if (mainActivity is MainActivity) {
                launchHandler.setFragmentManager(
                    mainActivity.supportFragmentManager
                )

                launchHandler.run()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}