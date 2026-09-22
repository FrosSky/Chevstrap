package com.chevstrap.rbx.ui.viewModels.settings

import androidx.lifecycle.ViewModel
import com.chevstrap.rbx.App
import com.chevstrap.rbx.extensions.ResourceManagerEx
import com.chevstrap.rbx.R

class SettingsScreenViewModel : ViewModel() {

    companion object {
        const val PAGE_INTEGRATIONS = "Integrations"
        const val PAGE_LAUNCHER = "Launcher"
        const val PAGE_SETTINGS = "Settings"
        const val PAGE_ABOUT = "About"
        const val PAGE_LOGGED = "Logged"
        const val PAGE_LOGIN_WEB = "LoginWeb"
        const val PAGE_LOG_VIEWER = "LogViewer"
    }

    var currentPage: String? = null
        private set

    var currentLogText: String? = null
        private set

    fun moveToPage(
        page: String,
        text: String? = null
    ): Boolean {

        setPage(page, text)
        return true
    }

    fun setPage(
        page: String?,
        text: String?,
    ): Boolean {

        if (page == currentPage) {
            return false
        }

        if (!isValidPage(page)) {
            return false
        }

        currentPage = page
        currentLogText = text

        return true
    }

    fun isValidPage(page: String?): Boolean {
        return when (page) {
            PAGE_INTEGRATIONS,
            PAGE_LAUNCHER,
            PAGE_SETTINGS,
            PAGE_ABOUT,
            PAGE_LOGGED,
            PAGE_LOGIN_WEB,
            PAGE_LOG_VIEWER -> true

            else -> false
        }
    }

    fun getPageTitle(page: String): String {
        return when (page) {

            PAGE_INTEGRATIONS -> {
                ResourceManagerEx.getStringOrEmpty(
                    App.appContext,
                    R.string.menu_integrations_title
                )
            }

            PAGE_LAUNCHER -> {
                ResourceManagerEx.getStringOrEmpty(
                    App.appContext,
                    R.string.menu_behaviour_title
                )
            }

            PAGE_SETTINGS -> {
                "Chevstrap"
            }

            PAGE_ABOUT -> {
                ResourceManagerEx.getStringOrEmpty(
                    App.appContext,
                    R.string.about_title
                )
            }

            PAGE_LOGGED -> {
                ResourceManagerEx.getStringOrEmpty(
                    App.appContext,
                    R.string.menu_discord_account_title
                )
            }

            PAGE_LOGIN_WEB -> {
                ResourceManagerEx.getStringOrEmpty(
                    App.appContext,
                    R.string.menu_discord_login_title
                )
            }

            PAGE_LOG_VIEWER -> {
                ResourceManagerEx.getStringOrEmpty(
                    App.appContext,
                    R.string.menu_logs_viewer_title
                )
            }

            else -> {
                throw IllegalArgumentException(
                    "Unknown page: $page"
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        currentPage = null
        currentLogText = null
    }
}