package chevstrap.preference

import android.content.Context
import chevstrap.extensions.FileTool
import org.json.JSONObject
import java.io.File

class FLKVManager private constructor(
    private val file: File
) {

    companion object {

        private const val DIRECTORY = "flkv"
        private const val FILE_NAME = "flkv.default"

        private var instance: FLKVManager? = null

        @JvmStatic
        fun defaultFLKV(
            context: Context
        ): FLKVManager {

            instance?.let {
                return it
            }

            val applicationContext = context.applicationContext

            val directory = File(
                applicationContext.filesDir,
                DIRECTORY
            )

            FileTool.ensureDirectoryExists(directory)

            val storageFile = File(
                directory,
                FILE_NAME
            )

            return FLKVManager(storageFile).also {
                instance = it
            }
        }
    }

    private val lock = Any()

    private val values = HashMap<String, Any?>()

    init {
        synchronized(lock) {
            load()
        }
    }

    fun encode(
        key: String,
        value: String?
    ): Boolean {
        synchronized(lock) {
            values[key] = value
            save()
        }

        return true
    }

    fun encode(
        key: String,
        value: Int
    ): Boolean {
        synchronized(lock) {
            values[key] = value
            save()
        }

        return true
    }

    fun encode(
        key: String,
        value: Boolean
    ): Boolean {
        synchronized(lock) {
            values[key] = value
            save()
        }

        return true
    }

    fun encode(
        key: String,
        value: Float
    ): Boolean {
        synchronized(lock) {
            values[key] = value
            save()
        }

        return true
    }

    fun encode(
        key: String,
        value: Long
    ): Boolean {
        synchronized(lock) {
            values[key] = value
            save()
        }

        return true
    }

    fun decodeString(
        key: String,
        defaultValue: String? = null
    ): String? {
        synchronized(lock) {
            return values[key] as? String ?: defaultValue
        }
    }

    fun decodeInt(
        key: String,
        defaultValue: Int = 0
    ): Int {
        synchronized(lock) {
            return values[key] as? Int ?: defaultValue
        }
    }

    fun decodeBool(
        key: String,
        defaultValue: Boolean = false
    ): Boolean {
        synchronized(lock) {
            return values[key] as? Boolean ?: defaultValue
        }
    }

    fun decodeFloat(
        key: String,
        defaultValue: Float = 0f
    ): Float {
        synchronized(lock) {
            return values[key] as? Float ?: defaultValue
        }
    }

    fun decodeLong(
        key: String,
        defaultValue: Long = 0L
    ): Long {
        synchronized(lock) {
            return values[key] as? Long ?: defaultValue
        }
    }

    fun removeValueForKey(
        key: String
    ): Boolean {
        synchronized(lock) {

            if (!values.containsKey(key)) {
                return false
            }

            values.remove(key)
            save()

            return true
        }
    }

    fun containsKey(
        key: String
    ): Boolean {
        synchronized(lock) {
            return values.containsKey(key)
        }
    }

    fun clearAll(): Boolean {
        synchronized(lock) {
            values.clear()
            save()

            return true
        }
    }

    private fun load() {
        if (!file.exists()) {
            return
        }

        try {
            val content = FileTool.safeRead(file)

            if (content.isBlank()) {
                return
            }

            val json = JSONObject(content)

            values.clear()

            val iterator = json.keys()

            while (iterator.hasNext()) {

                val key = iterator.next()

                when (val value = json.get(key)) {

                    JSONObject.NULL -> {
                        values[key] = null
                    }

                    is String -> {
                        values[key] = value
                    }

                    is Boolean -> {
                        values[key] = value
                    }

                    is Int -> {
                        values[key] = value
                    }

                    is Long -> {
                        values[key] = value
                    }

                    is Double -> {
                        values[key] = value.toFloat()
                    }
                }
            }

        } catch (_: Throwable) {
            values.clear()
        }
    }

    private fun save() {
        val json = JSONObject()

        for ((key, value) in values) {

            when (value) {

                null -> {
                    json.put(
                        key,
                        JSONObject.NULL
                    )
                }

                is String -> {
                    json.put(
                        key,
                        value
                    )
                }

                is Int -> {
                    json.put(
                        key,
                        value
                    )
                }

                is Boolean -> {
                    json.put(
                        key,
                        value
                    )
                }

                is Float -> {
                    json.put(
                        key,
                        value.toDouble()
                    )
                }

                is Long -> {
                    json.put(
                        key,
                        value
                    )
                }
            }
        }

        FileTool.safeWrite(
            file,
            json.toString()
        )
    }
}