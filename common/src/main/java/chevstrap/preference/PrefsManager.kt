package chevstrap.preference

import android.content.Context
import android.util.Base64
import chevstrap.extensions.EncryptionManager

object PrefsManager {

    private lateinit var kv: FLKVManager

    fun initialize(
        context: Context
    ) {
        if (!::kv.isInitialized) {
            kv = FLKVManager.defaultFLKV(
                context.applicationContext
            )
        }
    }

    fun setToken(
        token: String
    ) {
        ensureInitialized()

        val encrypted = EncryptionManager.encrypt(
            token.toByteArray()
        )

        set(
            TOKEN,
            Base64.encodeToString(
                encrypted,
                Base64.NO_WRAP
            )
        )
    }

    fun getToken(): String {
        ensureInitialized()

        val encoded = get(
            TOKEN,
            ""
        )

        if (encoded.isEmpty()) {
            return ""
        }

        return try {
            val encrypted = Base64.decode(
                encoded,
                Base64.NO_WRAP
            )

            val decrypted = EncryptionManager.decrypt(
                encrypted
            )

            String(
                decrypted,
                Charsets.UTF_8
            )
        } catch (_: Throwable) {
            ""
        }
    }

    fun getStatus(): String {
        ensureInitialized()

        return get(
            STATUS,
            ""
        )
    }

    fun setStatus(
        status: String
    ) {
        ensureInitialized()

        set(
            STATUS,
            status
        )
    }

    operator fun set(
        key: String,
        value: Any?
    ): Boolean {
        ensureInitialized()

        return when (value) {
            is String? ->
                kv.encode(
                    key,
                    value
                )

            is Int ->
                kv.encode(
                    key,
                    value
                )

            is Boolean ->
                kv.encode(
                    key,
                    value
                )

            is Float ->
                kv.encode(
                    key,
                    value
                )

            is Long ->
                kv.encode(
                    key,
                    value
                )

            else ->
                throw UnsupportedOperationException(
                    "Not yet implemented"
                )
        }
    }

    private inline operator fun <reified T : Any> get(
        key: String,
        defaultValue: T? = null
    ): T {
        ensureInitialized()

        return when (T::class) {

            String::class ->
                kv.decodeString(
                    key,
                    defaultValue as String? ?: ""
                ) as T

            Int::class ->
                kv.decodeInt(
                    key,
                    defaultValue as? Int ?: -1
                ) as T

            Boolean::class ->
                kv.decodeBool(
                    key,
                    defaultValue as? Boolean ?: false
                ) as T

            Float::class ->
                kv.decodeFloat(
                    key,
                    defaultValue as? Float ?: -1f
                ) as T

            Long::class ->
                kv.decodeLong(
                    key,
                    defaultValue as? Long ?: -1L
                ) as T

            else ->
                throw UnsupportedOperationException(
                    "Not yet implemented"
                )
        }
    }

    fun remove(
        key: String
    ) {
        ensureInitialized()

        kv.removeValueForKey(
            key
        )
    }

    fun ensureInitialized() {
        check(::kv.isInitialized) {
            "PrefsManager is not initialized. Call PrefsManager.initialize(context) first."
        }
    }

    const val TOKEN = "token"
    const val STATUS = "status"
}