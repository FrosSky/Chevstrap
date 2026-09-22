package com.chevstrap.rbx.appDirectories

import com.chevstrap.rbx.App

class RobloxClientData : CommonAppData() {
    override val executablePackage: String
        get() {
            val preferred: String =
                App.config.data.preferredRobloxApp
            return when (preferred) {
                "vng" -> {
                    "com.roblox.client.vnggames"
                }
                "galaxy_store" -> {
                    "com.roblox.client.samsunggalaxy"
                }
                "global" -> {
                    "com.roblox.client"
                }
                else -> {
                    "com.roblox.client"
                }
            }
        }
}