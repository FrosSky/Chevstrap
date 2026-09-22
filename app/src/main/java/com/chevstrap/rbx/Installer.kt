package com.chevstrap.rbx

import chevstrap.extensions.FileTool
import java.io.File

class Installer {
    fun handleUpgrades() {
        val currentVersion = App.getCurrentVersion(App.appContext)
        val targetVersion = "1.9"
        if (currentVersion == null) return

        val result = Utilities.compareVersions(targetVersion, currentVersion)

        if (result == Utilities.VersionComparison.LESS) {
            val clientAppSettingsOld =
                File(Paths.modifications, "ClientSettings/ClientAppSettings.json")
            if (FileTool.isExist(clientAppSettingsOld.toString())) {
                try {
                    FileTool.deleteFile(clientAppSettingsOld)
                } catch (_: Exception) {
                }
            }
            val lastClientAppSettingsOld =
                File(Paths.modifications, "ClientSettings/LastClientAppSettings.json")
            if (FileTool.isExist(lastClientAppSettingsOld.toString())) {
                try {
                    FileTool.deleteFile(lastClientAppSettingsOld)
                } catch (_: Exception) {
                }
            }
            FileTool.deleteDir(File(Paths.modifications), true)
        }
    }
}