package com.chevstrap.rbx.appDirectories

import android.content.pm.PackageManager
import com.chevstrap.rbx.App
import java.io.File

abstract class CommonAppData {
    abstract val executablePackage: String?
    val executablePath: String?
        get() {
            try {
                val robloxDir = this.robloxDirectory
                if (robloxDir != null) {
                    return File(robloxDir, "appData").absolutePath
                }
            } catch (e: Exception) {
                App.logger.writeException("CommonAppData::getExecutablePath", e)
            }
            return null
        }

    val robloxDirectory: String?
        get() = getRobloxDirectory(includeFiles = true)

    fun getRobloxDirectory(includeFiles: Boolean): String? {
        val packageName = this.executablePackage ?: return null

        try {
            val dataDir = App.appContext!!
                .packageManager
                .getApplicationInfo(packageName, 0)
                .dataDir

            if (dataDir != null) {
                return if (includeFiles) {
                    File(dataDir, "files").absolutePath
                } else {
                    File(dataDir).absolutePath
                }
            } else {
                App.logger.writeLine(
                    "CommonAppData::getRobloxDirectory",
                    "dataDir is null"
                )
            }
        } catch (e: PackageManager.NameNotFoundException) {
            App.logger.writeLine(
                "CommonAppData::getRobloxDirectory",
                "failed getting package"
            )
            App.logger.writeException(
                "CommonAppData::getRobloxDirectory",
                e
            )
        }

        val fallback = App.appContext!!.filesDir.absolutePath
            .replace(
                "/" + App.appContext!!.packageName + "/",
                "/$packageName/"
            )

        return if (includeFiles) {
            "$fallback/"
        } else {
            fallback.removeSuffix("/files")
        }
    }
}
