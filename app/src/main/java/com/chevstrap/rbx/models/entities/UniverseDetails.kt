package com.chevstrap.rbx.models.entities

import android.os.Handler
import android.os.Looper
import com.chevstrap.rbx.App
import com.chevstrap.rbx.models.apis.roblox.ApiArrayResponse
import com.chevstrap.rbx.models.apis.roblox.GameDetailResponse
import com.chevstrap.rbx.models.apis.roblox.ThumbnailResponse
import com.chevstrap.rbx.utility.HTTPFetcher
import kotlinx.serialization.json.Json

class UniverseDetails {

    var data: GameDetailResponse? = null
        private set

    var thumbnail: ThumbnailResponse? = null
        private set

    interface FetchCallback {
        fun onFetched(universeDetails: UniverseDetails?)
        fun onFailed(e: Exception?)
    }

    companion object {

        private val json = Json {
            encodeDefaults = false
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        fun fetchSingle(universeId: Long, callback: FetchCallback) {
            Thread {
                try {
                    val gameResponse = HTTPFetcher.getJson(
                        "https://games.roblox.com/v1/games?universeIds=$universeId"
                    ).toString()

                    val gameData = json.decodeFromString<ApiArrayResponse<GameDetailResponse>>(
                        gameResponse
                    ).data.firstOrNull()
                        ?: throw Exception("Roblox API returned invalid game data")

                    val thumbResponse = HTTPFetcher.getJson(
                        "https://thumbnails.roblox.com/v1/games/icons?universeIds=$universeId" +
                                "&returnPolicy=PlaceHolder" +
                                "&size=512x512" +
                                "&format=Png" +
                                "&isCircular=false"
                    ).toString()

                    val thumbnailData = json.decodeFromString<ApiArrayResponse<ThumbnailResponse>>(
                        thumbResponse
                    ).data.firstOrNull()
                        ?: throw Exception("Thumbnail object missing in response")

                    val universeDetails = UniverseDetails().apply {
                        data = gameData
                        thumbnail = thumbnailData
                    }

                    Handler(Looper.getMainLooper()).post {
                        callback.onFetched(universeDetails)
                    }
                } catch (e: Exception) {
                    App.logger.writeException("UniverseDetails", e)

                    Handler(Looper.getMainLooper()).post {
                        callback.onFailed(e)
                    }
                }
            }.start()
        }
    }
}