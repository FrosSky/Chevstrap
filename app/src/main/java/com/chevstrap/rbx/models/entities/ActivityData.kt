package com.chevstrap.rbx.models.entities

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.core.net.toUri
import com.chevstrap.rbx.App
import com.chevstrap.rbx.enums.ServerType
import com.chevstrap.rbx.models.apis.IPInfoResponse
import com.chevstrap.rbx.models.entities.UniverseDetails.FetchCallback
import kotlinx.serialization.json.Json
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Semaphore

class ActivityData(machineAddress: String? = "") {

    var universeId: Long = 0L

    fun setUniverseId(value: Long, callback: (Boolean) -> Unit) {
        universeId = value
        App.logger.writeLine(LOG_IDENTIFIER, "Universe ID set to $value")

        if (universeDetails != null) {
            callback(true)
            return
        }

        UniverseDetails.fetchSingle(value, object : FetchCallback {
            override fun onFetched(universeDetails: UniverseDetails?) {
                this@ActivityData.universeDetails = universeDetails

                val detailsData = universeDetails?.data
                if (detailsData == null) {
                    callback(false)
                    return
                }

                if (lastUniverseId == detailsData.id) {
                    isTeleport = true
                }

                rootPlaceId = detailsData.rootPlaceId
                isUniverseDetailsFetched = true

                callback(true)
            }

            override fun onFailed(e: Exception?) {
                isUniverseDetailsFetched = false
                App.logger.writeLine(LOG_IDENTIFIER, "Failed to fetch universe details")
                App.logger.writeException(LOG_IDENTIFIER, e)

                callback(false)
            }
        })
    }

    var rootPlaceId: Long = 0
        set(value) {
            field = value
            App.logger.writeLine(LOG_IDENTIFIER, "Root place ID set to $value")
        }

    private val lastUniverseId: Long = 0

    var placeId: Long = 0
        set(value) {
            field = value
            App.logger.writeLine(LOG_IDENTIFIER, "Place ID set to $value")
        }

    var userId: Long = 0
        set(value) {
            field = value
            App.logger.writeLine(LOG_IDENTIFIER, "User ID set to $value")
        }

    var jobId: String? = ""
        set(value) {
            field = value
            App.logger.writeLine(LOG_IDENTIFIER, "Job ID set to $value")
        }

    var rpcLaunchData: String? = ""
        set(value) {
            field = value
            App.logger.writeLine(LOG_IDENTIFIER, "RPC Launch Data set to $value")
        }

    var machineAddress: String? = machineAddress
        set(value) {
            field = value
            App.logger.writeLine(LOG_IDENTIFIER, "Machine address set to $value")
        }

    var isTeleport: Boolean = false
        set(value) {
            field = value
            App.logger.writeLine(LOG_IDENTIFIER, "Teleport flag = $value")
        }

    var serverType: ServerType = ServerType.PUBLIC
        set(value) {
            field = value
            App.logger.writeLine(LOG_IDENTIFIER, "Server type = $value")
        }

    var isUniverseDetailsFetched: Boolean = false
        set(value) {
            field = value
            App.logger.writeLine(LOG_IDENTIFIER, "Universe Details Fetched = $value")
        }

    var timeJoined: Date? = null
        set(value) {
            field = value
            App.logger.writeLine(LOG_IDENTIFIER, "Time joined = $value")
        }

    var timeLeft: Date? = null
        set(value) {
            field = value
            App.logger.writeLine(LOG_IDENTIFIER, "Time left = $value")
        }

    @JvmField
    var universeDetails: UniverseDetails? = null

    private val serverQuerySemaphore = Semaphore(1)

    val isMachineAddressValid: Boolean
        get() {
            val address = machineAddress
            val valid = !address.isNullOrEmpty() && !address.startsWith("10.")
            App.logger.writeLine(LOG_IDENTIFIER, "Machine address valid = $valid")
            return valid
        }
    
    val gameHistoryDescription: String
        get() {
            val timeFormat = DateFormat.getTimeInstance(
                DateFormat.SHORT,
                Locale.getDefault()
            )

            val creatorName = universeDetails?.data?.creator?.name.orEmpty()

            val joinedText = timeJoined?.let(timeFormat::format) ?: "N/A"
            val leftText = timeLeft?.let(timeFormat::format) ?: "N/A"

            val desc = "$creatorName • $joinedText - $leftText"

            App.logger.writeLine(
                LOG_IDENTIFIER,
                "Game history description generated: $desc"
            )
            App.logger.writeLine(
                LOG_IDENTIFIER,
                "Creator name: $creatorName"
            )

            return desc
        }

    fun getInviteDeeplink(privateServerAccessCode: String?): String {
        val deeplink = StringBuilder("roblox://experiences/start?placeId=$placeId")
        if (!privateServerAccessCode.isNullOrEmpty()) {
            deeplink.append("&accessCode=").append(privateServerAccessCode)
        } else if (!jobId.isNullOrEmpty()) {
            deeplink.append("&gameInstanceId=").append(jobId)
        }
        if (!rpcLaunchData.isNullOrEmpty())
            deeplink.append("&launchData=" + URLEncoder.encode(rpcLaunchData, "UTF-8"))
        val link = deeplink.toString()
        App.logger.writeLine(LOG_IDENTIFIER, "Generated deeplink: $link")
        return link
    }

    private fun fetchIPInfoIo(ip: String?, callback: IPInfoCallback) {
        App.logger.writeLine(LOG_IDENTIFIER, "Fetching IP info for $ip")
        Thread {
            try {
                val url = URL("https://ipinfo.io/$ip/json")
                val json: JSONObject = getJsonObject(url)
                Handler(Looper.getMainLooper()).post { callback.onSuccess(json) }
                App.logger.writeLine(LOG_IDENTIFIER, "IP info fetch success for $ip")
            } catch (e: Exception) {
                App.logger.writeException(LOG_IDENTIFIER, e)
                Handler(Looper.getMainLooper()).post { callback.onError(e) }
            }
        }.start()
    }

    interface IPInfoCallback {
        fun onSuccess(json: JSONObject?)
        fun onError(e: Exception?)
    }

    interface LocationCallback {
        fun onLocationResolved(location: String?)
        fun onFailure()
    }

    fun queryServerLocation(callback: LocationCallback) {
        if (!this.isMachineAddressValid) {
            App.logger.writeLine(LOG_IDENTIFIER, "Invalid machine address, cannot resolve server location")
            callback.onFailure()
            return
        }

        Thread {
            try {
                serverQuerySemaphore.acquire()

                fetchIPInfoIo(machineAddress, object : IPInfoCallback {
                    override fun onSuccess(json: JSONObject?) {
                        try {
                            if (json == null) {
                                App.logger.writeLine(
                                    LOG_IDENTIFIER,
                                    "Failed to parse IP info response"
                                )
                                callback.onFailure()
                                return
                            }

                            val ipInfo = serializerJson.decodeFromString<IPInfoResponse>(
                                json.toString()
                            )

                            val city = ipInfo.city
                            val region = ipInfo.region
                            val country = ipInfo.country

                            if (city?.isBlank() == true) {
                                App.logger.writeLine(
                                    LOG_IDENTIFIER,
                                    "Invalid IP info: city missing"
                                )
                                callback.onFailure()
                                return
                            }

                            val location = if (city == region) {
                                "$region, $country"
                            } else {
                                "$city, $region, $country"
                            }

                            App.logger.writeLine(
                                LOG_IDENTIFIER,
                                "Resolved server location: $location"
                            )

                            callback.onLocationResolved(location)
                        } catch (e: Exception) {
                            App.logger.writeException(LOG_IDENTIFIER, e)
                            callback.onFailure()
                        } finally {
                            serverQuerySemaphore.release()
                        }
                    }

                    override fun onError(e: Exception?) {
                        try {
                            if (e != null) {
                                App.logger.writeException(LOG_IDENTIFIER, e)
                            }

                            callback.onFailure()
                        } finally {
                            serverQuerySemaphore.release()
                        }
                    }
                })
            } catch (e: InterruptedException) {
                App.logger.writeException(LOG_IDENTIFIER, e)
                callback.onFailure()
            } catch (e: Exception) {
                App.logger.writeException(LOG_IDENTIFIER, e)
                callback.onFailure()
            }
        }.start()
    }

    fun rejoinServer(context: Context) {
        App.logger.writeLine(LOG_IDENTIFIER, "Rejoining server via deeplink: ${getInviteDeeplink("")}")
        val intent = Intent(Intent.ACTION_VIEW, getInviteDeeplink("").toUri())
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    companion object {
        private const val LOG_IDENTIFIER = "ActivityData"

        private val serializerJson = Json {
            encodeDefaults = false
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        @Throws(IOException::class, JSONException::class)
        private fun getJsonObject(url: URL): JSONObject {
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val reader = BufferedReader(
                InputStreamReader(connection.inputStream)
            )

            val result = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                result.append(line)
            }

            reader.close()

            return JSONObject(result.toString())
        }
    }
}
