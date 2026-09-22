package com.chevstrap.rbx.models.entities

import chevstrap.preference.PrefsManager
import com.chevstrap.rbx.App
import com.chevstrap.rbx.utility.HTTPFetcher
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min
import kotlin.math.roundToLong
import kotlin.time.Duration.Companion.milliseconds

class ExternalAssetFetcher {

    companion object {
        private const val MAX_RETRIES = 3
        private const val MAX_CACHE_SIZE = 100
        private const val DEFAULT_RETRY_DELAY_MS = 1000L
        private const val MAX_RETRY_DELAY_MS = 30_000L

        private val cache =
            ConcurrentHashMap<String, String>()

        @Volatile
        private var rateLimitUntil = 0L
    }

    suspend fun fetchMPOfUrl(
        applicationId: String?,
        url: String?
    ): String? {

        val tag = "ExternalAssetFetcher::fetchMPOfUrl"
        if (applicationId.isNullOrBlank()) {
            App.logger.writeLine(
                tag,
                "Application ID is empty"
            )
            return null
        }

        if (url.isNullOrBlank()) {
            App.logger.writeLine(
                tag,
                "URL is empty"
            )
            return null
        }

        val token = PrefsManager.getToken()
        if (token.isBlank()) {
            App.logger.writeLine(
                tag,
                "Discord token is unavailable"
            )
            return null
        }

        val cacheKey = "$applicationId|$url"

        cache[cacheKey]?.let { cached ->
            App.logger.writeLine(
                tag,
                "Using cached media proxy"
            )

            return cached
        }

        waitForRateLimit()

        val urls = JSONArray().apply {
            put(url)
        }

        val json = JSONObject().apply {
            put("urls", urls)
        }

        var attempt = 0

        while (true) {
            attempt++

            try {
                val response = HTTPFetcher.postJson(
                    url = "https://discord.com/api/v9/applications/$applicationId/external-assets",
                    json = json,
                    headers = mapOf(
                        "Authorization" to token,
                        "Content-Type" to "application/json"
                    )
                )

                if (response.code == 429) {
                    val retryAfter = parseRetryAfter(
                        response.body,
                        response.headers["Retry-After"]
                    )

                    val delayMs = retryAfter.coerceIn(
                        DEFAULT_RETRY_DELAY_MS,
                        MAX_RETRY_DELAY_MS
                    )

                    rateLimitUntil =
                        System.currentTimeMillis() + delayMs

                    App.logger.writeLine(
                        tag,
                        "Discord rate limited request " +
                                "(attempt $attempt/$MAX_RETRIES), " +
                                "retrying after ${delayMs}ms"
                    )

                    if (attempt >= MAX_RETRIES) {
                        App.logger.writeLine(
                            tag,
                            "Maximum retries reached after HTTP 429"
                        )

                        return null
                    }

                    delay(delayMs.milliseconds)
                    continue
                }

                if (response.code !in 200..299) {
                    App.logger.writeLine(
                        tag,
                        "HTTP ${response.code}"
                    )

                    return null
                }

                val responseBody = response.body

                if (responseBody.isBlank()) {
                    App.logger.writeLine(
                        tag,
                        "Discord returned an empty response"
                    )

                    return null
                }

                val array = JSONArray(responseBody)

                if (array.length() == 0) {
                    App.logger.writeLine(
                        tag,
                        "Discord returned an empty asset array"
                    )

                    return null
                }

                val obj = array.optJSONObject(0)

                if (obj == null) {
                    App.logger.writeLine(
                        tag,
                        "Invalid Discord asset response"
                    )

                    return null
                }

                val externalAssetPath =
                    obj.optString(
                        "external_asset_path",
                        ""
                    )

                if (externalAssetPath.isBlank()) {
                    App.logger.writeLine(
                        tag,
                        "external_asset_path is missing"
                    )

                    return null
                }

                val result =
                    "mp:$externalAssetPath"

                putCache(
                    cacheKey,
                    result
                )

                App.logger.writeLine(
                    tag,
                    "Media proxy resolved successfully"
                )

                return result

            } catch (e: Exception) {

                App.logger.writeLine(
                    tag,
                    "Request failed: ${e.javaClass.simpleName}"
                )

                if (attempt >= MAX_RETRIES) {
                    App.logger.writeLine(
                        tag,
                        "Maximum retries reached"
                    )

                    return null
                }

                val retryDelay =
                    calculateBackoff(attempt)

                App.logger.writeLine(
                    tag,
                    "Retrying after ${retryDelay}ms"
                )

                delay(retryDelay.milliseconds)
            }
        }
    }

    private suspend fun waitForRateLimit() {
        while (true) {
            val tag = "ExternalAssetFetcher::waitForRateLimit"
            val remaining =
                rateLimitUntil -
                        System.currentTimeMillis()

            if (remaining <= 0) {
                return
            }

            App.logger.writeLine(
                tag,
                "Rate-limit cooldown active, " +
                        "waiting ${remaining}ms"
            )

            delay(remaining.milliseconds)
        }
    }

    private fun parseRetryAfter(
        responseBody: String?,
        retryAfterHeader: String?
    ): Long {

        if (!responseBody.isNullOrBlank()) {
            try {

                val json =
                    JSONObject(responseBody)

                if (json.has("retry_after")) {

                    val retryAfter =
                        json.optDouble(
                            "retry_after",
                            -1.0
                        )

                    if (retryAfter >= 0) {
                        return (
                                retryAfter * 1000.0
                                ).roundToLong()
                    }
                }

            } catch (_: Exception) {
            }
        }

        if (!retryAfterHeader.isNullOrBlank()) {
            try {

                val seconds =
                    retryAfterHeader.toDouble()

                if (seconds >= 0) {
                    return (
                            seconds * 1000.0
                            ).roundToLong()
                }

            } catch (_: NumberFormatException) {
            }
        }

        return DEFAULT_RETRY_DELAY_MS
    }

    private fun calculateBackoff(
        attempt: Int
    ): Long {

        val exponent =
            (attempt - 1).coerceAtLeast(0)

        return min(
            500L * (1L shl exponent),
            MAX_RETRY_DELAY_MS
        )
    }

    private fun putCache(
        key: String,
        value: String
    ) {

        if (cache.size >= MAX_CACHE_SIZE) {

            val firstKey =
                cache.keys.firstOrNull()

            if (firstKey != null) {
                cache.remove(firstKey)
            }
        }

        cache[key] = value
    }

    fun clearCache() {
        cache.clear()
    }

    fun invalidate(
        applicationId: String?,
        url: String?
    ) {

        if (
            applicationId.isNullOrBlank() ||
            url.isNullOrBlank()
        ) {
            return
        }

        cache.remove(
            "$applicationId|$url"
        )
    }
}