package com.chevstrap.rbx.utility

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object HTTPFetcher {

    private val client = OkHttpClient()

    @JvmStatic
    @Throws(IOException::class, JSONException::class)
    fun getJson(
        urlString: String,
        headers: Map<String, String> = emptyMap()
    ): Any {
        var connection: HttpURLConnection? = null
        var reader: BufferedReader? = null

        try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            connection.setRequestProperty(
                "Accept",
                "application/json"
            )

            headers.forEach { (key, value) ->
                connection.setRequestProperty(key, value)
            }

            val responseCode = connection.responseCode

            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException(
                    "HTTP request failed with code $responseCode"
                )
            }

            reader = BufferedReader(
                InputStreamReader(connection.inputStream)
            )

            val responseBuilder = StringBuilder()

            var line: String?

            while (reader.readLine().also { line = it } != null) {
                responseBuilder.append(line)
            }

            val json = responseBuilder
                .toString()
                .trim()

            return if (json.startsWith("[")) {
                JSONArray(json)
            } else {
                JSONObject(json)
            }

        } finally {
            reader?.close()
            connection?.disconnect()
        }
    }

    @JvmStatic
    @Throws(IOException::class, JSONException::class)
    fun postJson(
        url: String,
        json: Any,
        headers: Map<String, String> = emptyMap()
    ): HttpResponse {

        val jsonString = when (json) {
            is JSONObject -> json.toString()
            is JSONArray -> json.toString()
            else -> throw IllegalArgumentException(
                "JSON must be JSONObject or JSONArray"
            )
        }

        val body = jsonString.toRequestBody(
            "application/json; charset=utf-8".toMediaType()
        )

        val requestBuilder = Request.Builder()
            .url(url)
            .post(body)

        headers.forEach { (key, value) ->
            requestBuilder.header(key, value)
        }

        client.newCall(
            requestBuilder.build()
        ).execute().use { response ->

            return HttpResponse(
                code = response.code,
                body = response.body.string(),
                headers = response.headers.toMap()
            )
        }
    }

    data class HttpResponse(
        val code: Int,
        val body: String,
        val headers: Map<String, String>
    )
}