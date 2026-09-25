package com.chevstrap.rbx

import com.chevstrap.rbx.enums.DiscordStatusType
import com.chevstrap.rbx.enums.LaunchMode
import com.chevstrap.rbx.enums.ThemeRecreated
import com.chevstrap.rbx.models.persistable.ConfigData
import kotlinx.serialization.KSerializer
import java.io.File
import kotlin.reflect.KProperty1

class ConfigManager private constructor() : JsonManager<ConfigData>() {

    private data class Listener(
        val property: KProperty1<ConfigData, *>,
        val callback: (ConfigData) -> Unit
    )

    private val listeners = mutableListOf<Listener>()
    private var lastNotifiedData: ConfigData? = null

    init {
        prop = ConfigData()
        originalProp = ConfigData()
        lastNotifiedData = ConfigData()
    }

    override val serializer: KSerializer<ConfigData>
        get() = ConfigData.serializer()

    override fun createDefault(): ConfigData {
        return ConfigData()
    }

    override val className: String
        get() = "ConfigManager"

    override val fileLocation: File
        get() {
            val logIdentifier = "ConfigManager::fileLocation"
            val dir = File(Paths.localAppData)

            if (!dir.exists() && !dir.mkdirs()) {
                App.logger.writeLine(
                    logIdentifier,
                    "Could not create directory: ${dir.absolutePath}"
                )
            }

            return File(dir, "Config.json")
        }

    val data: ConfigData
        get() = prop!!

    val originalData: ConfigData
        get() = originalProp!!

    fun hasUnsavedChanges(): Boolean {
        return data != originalData
    }

    fun <T> listen(
        property: KProperty1<ConfigData, T>,
        callback: (T) -> Unit
    ) {
        val listener = Listener(
            property = property,
            callback = { config ->
                callback(property.get(config))
            }
        )

        synchronized(listeners) {
            listeners.add(listener)
        }
    }

    fun clearListeners() {
        synchronized(listeners) {
            listeners.clear()
        }
    }

    fun checkForChanges(): Boolean {
        val current = data
        val previous = lastNotifiedData ?: originalData

        if (current == previous) {
            return false
        }

        notifyListeners(
            current = current,
            previous = previous
        )
        lastNotifiedData = current.copy()
        return true
    }

    private fun notifyListeners(
        current: ConfigData,
        previous: ConfigData
    ) {
        val snapshot = synchronized(listeners) {
            listeners.toList()
        }

        snapshot.forEach { listener ->
            val currentValue = listener.property.get(current)
            val previousValue = listener.property.get(previous)

            if (currentValue != previousValue) {
                listener.callback(current)
            }
        }
    }

    override fun load(alertFailure: Boolean) {
        super.load(alertFailure)

        val snapshot = data.copy()
        originalProp = snapshot
        lastNotifiedData = snapshot.copy()
    }

    override fun save() {
        super.save()

        val snapshot = data.copy()
        originalProp = snapshot
        lastNotifiedData = snapshot.copy()
    }

    companion object {

        @JvmField
        val ThemeRecreates: Map<ThemeRecreated, String> = mapOf(
            ThemeRecreated.Dark to "dark",
            ThemeRecreated.Light to "light"
        )

        @JvmField
        val DiscordOnlineStatus: Map<DiscordStatusType, String> = mapOf(
            DiscordStatusType.Online to "online",
            DiscordStatusType.DoNotDisturb to "dnd",
            DiscordStatusType.Idle to "idle"
        )

        @JvmField
        val RobloxAppTypes: Map<LaunchMode, String> = mapOf(
            LaunchMode.Global to "global",
            LaunchMode.VNG to "vng",
            LaunchMode.GalaxyStore to "galaxy_store"
        )

        @Volatile
        private var instanceDelegate: ConfigManager? = null

        @JvmStatic
        val instance: ConfigManager
            get() {
                return instanceDelegate ?: synchronized(this) {
                    instanceDelegate
                        ?: ConfigManager().also {
                            instanceDelegate = it
                        }
                }
            }
    }
}