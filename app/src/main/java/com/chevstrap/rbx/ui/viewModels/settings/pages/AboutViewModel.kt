package com.chevstrap.rbx.ui.viewModels.settings.pages

import com.chevstrap.rbx.App
import com.chevstrap.rbx.ui.viewModels.GlobalViewModel

class AboutViewModel {

    fun fsPage() {
        GlobalViewModel.openWebpage(
            App.appContext,
            "https://github.com/FrosSky"
        )
    }

    fun discordServerPage() {
        GlobalViewModel.openWebpage(
            App.appContext,
            App.DISCORD_SERVER_INVITE_LINK
        )
    }

    fun projectWikiPage() {
        GlobalViewModel.openWebpage(
            App.appContext,
            App.PROJECT_WIKI
        )
    }

    fun projectRepositoryPage() {
        GlobalViewModel.openWebpage(
            App.appContext,
            "https://github.com/" + App.PROJECT_REPOSITORY
        )
    }

    fun bsPage() {
        GlobalViewModel.openWebpage(
            App.appContext,
            "https://github.com/bloxstraplabs/bloxstrap/tree/main/Bloxstrap"
        )
    }
}