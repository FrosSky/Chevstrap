package com.chevstrap.rbx.utility

import android.util.Log
import com.chevstrap.rbx.models.apis.roblox.ThumbnailBatchResponse
import com.chevstrap.rbx.models.apis.roblox.ThumbnailRequest
import com.chevstrap.rbx.models.apis.roblox.ThumbnailResponse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONArray
import java.io.IOException

object Thumbnails {

    private const val TAG = "Thumbnails:AA"
    private const val URL = "https://thumbnails.roblox.com/v1/batch"
    private const val RETRIES = 5
    private const val RETRY_TIME_INCREMENT = 500L

    private val json = Json {
        encodeDefaults = false
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Throws(IOException::class, InterruptedException::class)
    fun getThumbnailUrls(
        requests: MutableList<ThumbnailRequest>
    ): Array<String?> {
        val urls = arrayOfNulls<String>(requests.size)

        for (i in requests.indices) {
            requests[i].requestId = i.toString()
        }

        val jsonString: String = json.encodeToString(requests)
        val payload = JSONArray(jsonString)

        for (retry in 1..RETRIES) {
            val response = post(payload)

            val finished = response.none {
                it.state == "Pending"
            }

            if (finished) {
                for (item in response) {
                    when (item.state) {
                        "Pending" -> {
                            Log.w(
                                TAG,
                                "${item.targetId} is still pending"
                            )
                        }

                        "Error" -> {
                            Log.e(
                                TAG,
                                "${item.targetId} got error code " +
                                        "${item.errorCode} (${item.errorMessage})"
                            )
                        }

                        "Completed" -> Unit

                        else -> {
                            Log.w(
                                TAG,
                                "${item.targetId} got \"${item.state}\""
                            )
                        }
                    }

                    val index = item.requestId?.toIntOrNull()

                    if (index != null && index in urls.indices) {
                        urls[index] = item.imageUrl
                    }
                }

                return urls
            }

            if (retry == RETRIES) {
                Log.w(TAG, "Ran out of retries")
            } else {
                Thread.sleep(RETRY_TIME_INCREMENT * retry)
            }
        }

        return urls
    }

    @JvmStatic
    @Throws(IOException::class, InterruptedException::class)
    fun getThumbnailUrl(
        request: ThumbnailRequest
    ): String? {
        request.requestId = "0"

        val jsonString: String = json.encodeToString(
            listOf(request)
        )

        val payload = JSONArray(jsonString)

        var response: ThumbnailResponse? = null

        for (retry in 1..RETRIES) {
            response = post(payload).firstOrNull()
                ?: throw IOException(
                    "Thumbnail API returned empty response"
                )

            if (response.state != "Pending") {
                break
            }

            if (retry == RETRIES) {
                Log.w(TAG, "Ran out of retries")
            } else {
                Thread.sleep(RETRY_TIME_INCREMENT * retry)
            }
        }

        response ?: return null

        when (response.state) {
            "Pending" -> {
                Log.w(
                    TAG,
                    "${response.targetId} is still pending"
                )
            }

            "Error" -> {
                Log.e(
                    TAG,
                    "${response.targetId} got error code " +
                            "${response.errorCode} (${response.errorMessage})"
                )
            }

            "Completed" -> Unit

            else -> {
                Log.w(
                    TAG,
                    "${response.targetId} got \"${response.state}\""
                )
            }
        }

        return response.imageUrl
    }

    @Throws(IOException::class)
    private fun post(
        payload: JSONArray
    ): Array<ThumbnailResponse> {
        val response = HTTPFetcher.postJson(
            url = URL,
            json = payload,
            headers = mapOf(
                "Content-Type" to "application/json"
            )
        )

        if (response.code !in 200..299) {
            throw IOException("HTTP ${response.code}")
        }

        val result: ThumbnailBatchResponse =
            json.decodeFromString<ThumbnailBatchResponse>(
                response.body
            )

        return result.data.toTypedArray()
    }
}