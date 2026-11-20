package com.chevstrap.rbx

import chevstrap.extensions.FileTool
import org.json.JSONObject
import java.io.File

object FastFlagsManager {

    val presetKeys: List<String> = listOf(
        "FIntDebugForceMSAASamples",
        "FFlagDebugGraphicsPreferD3D11",
        "FFlagDebugGraphicsPreferD3D11FL10",
        "FFlagDebugGraphicsPreferVulkan",
        "FFlagDebugGraphicsPreferOpenGL",
        "FFlagDebugGraphicsDisableDirect3D11",
        "FIntRenderShadowIntensity",
        "FIntRenderShadowmapBias",
        "DFIntBandwidthManagerApplicationDefaultBps",
        "DFIntBandwidthManagerDataSenderMaxWorkCatchupMs",
        "DFIntAssetPreloading",
        "DFIntNumAssetsMaxToPreload",
        "DFIntConnectionMTUSize",
        "FFlagDisablePostFx",
        "FIntFRMMaxGrassDistance",
        "FIntRenderGrassDetailStrands",
        "FIntFRMMinGrassDistance",
        "FFlagDebugDisplayFPS",
        "DFIntTaskSchedulerTargetFps",
        "FFlagRenderUnifiedLighting12",
        "FIntRobloxGuiBlurIntensity"
    )

    val fileLocation: File
        get() {
            val filesDir = requireNotNull(App.appContext?.filesDir) {
                "App context is not ready"
            }
            return File(filesDir, "FastFlags/ClientAppSettings.json")
        }

    fun load(): JSONObject {
        val file = fileLocation
        if (!file.exists()) {
            return JSONObject()
        }

        return try {
            JSONObject(FileTool.safeRead(file))
        } catch (e: Exception) {
            throw IllegalStateException(
                "Failed to read Fast Flags from ${file.absolutePath}",
                e
            )
        }
    }

    fun save(json: JSONObject) {
        try {
            FileTool.safeWrite(fileLocation, json.toString(4))
        } catch (e: Exception) {
            throw IllegalStateException(
                "Failed to write Fast Flags to ${fileLocation.absolutePath}",
                e
            )
        }
    }

    fun set(name: String, rawValue: String) {
        require(name.isNotBlank()) { "Flag name cannot be empty" }

        val value = rawValue.toIntOrNull()
            ?: rawValue.toLongOrNull()
            ?: rawValue.toDoubleOrNull()
            ?: rawValue.toBooleanStrictOrNull()
            ?: rawValue

        val json = load()
        json.put(name, value)
        save(json)
    }

    fun remove(name: String) {
        val json = load()
        json.remove(name)
        save(json)
    }

    fun import(json: JSONObject) {
        val merged = load()
        val keys = json.keys()

        while (keys.hasNext()) {
            val key = keys.next()
            require(key.isNotBlank()) { "Imported flag names cannot be empty" }

            val value = json.get(key)
            require(value is String || value is Number || value is Boolean) {
                "Flag '$key' must have a string, number, or boolean value"
            }

            merged.put(key, value)
        }

        save(merged)
    }

    fun export(): String = load().toString(4)
}
