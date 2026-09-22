package com.chevstrap.rbx.models.entities

import com.chevstrap.rbx.models.apis.roblox.ApiArrayResponse
import com.chevstrap.rbx.models.apis.roblox.GetUserResponse
import com.chevstrap.rbx.models.apis.roblox.ThumbnailResponse
import kotlinx.serialization.json.Json
import org.json.JSONException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class UserDetails {
    @JvmField
    var data: GetUserResponse? = null

    @JvmField
    var thumbnail: ThumbnailResponse? = null

    companion object {
        private val CACHE: ConcurrentMap<Long, UserDetails> =
            ConcurrentHashMap()

        private const val MAX_CACHE_SIZE = 100

        private val json = Json {
            encodeDefaults = false
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        @JvmStatic
        @Throws(IOException::class, JSONException::class)
        fun fetch(id: Long): UserDetails {
            val cached = CACHE[id]
            if (cached != null) {
                return cached
            }

            val userResponse = json.decodeFromString<GetUserResponse>(
                fetchJson("https://users.roblox.com/v1/users/$id")
            )

            val thumbnailResponse =
                json.decodeFromString<ApiArrayResponse<ThumbnailResponse>>(
                    fetchJson(
                        "https://thumbnails.roblox.com/v1/users/avatar-headshot" +
                                "?userIds=$id" +
                                "&size=180x180" +
                                "&format=Png" +
                                "&isCircular=false"
                    )
                )

            val details = UserDetails()
            details.data = userResponse

            if (thumbnailResponse.data.isNotEmpty()) {
                details.thumbnail = thumbnailResponse.data[0]
            }

            if (CACHE.size >= MAX_CACHE_SIZE) {
                val firstKey = CACHE.keys.firstOrNull()

                if (firstKey != null) {
                    CACHE.remove(firstKey)
                }
            }

            CACHE[id] = details
            return details
        }

        private fun fetchJson(url: String): String {
            val connection =
                URL(url).openConnection() as HttpURLConnection

            try {
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")

                BufferedReader(
                    InputStreamReader(
                        connection.inputStream,
                        StandardCharsets.UTF_8
                    )
                ).use { reader ->
                    return reader.readText()
                }
            } finally {
                connection.disconnect()
            }
        }

        fun removeFromCache(id: Long) {
            CACHE.remove(id)
        }

        fun clearCache() {
            CACHE.clear()
        }

        fun isCached(id: Long): Boolean {
            return CACHE.containsKey(id)
        }
    }
}