package com.chevstrap.rbx.integrations

import chevstrap.gateway.DiscordWsImplementation
import chevstrap.gateway.DiscordWsManager
import chevstrap.preference.PrefsManager
import com.chevstrap.rbx.App
import com.chevstrap.rbx.enums.ServerType
import com.chevstrap.rbx.models.apis.roblox.ThumbnailRequest
import com.chevstrap.rbx.models.chevstrapRPC.Activity
import com.chevstrap.rbx.models.chevstrapRPC.Assets
import com.chevstrap.rbx.models.chevstrapRPC.Buttons
import com.chevstrap.rbx.models.chevstrapRPC.Metadata
import com.chevstrap.rbx.models.chevstrapRPC.Timestamps
import com.chevstrap.rbx.models.entities.ActivityData
import com.chevstrap.rbx.models.entities.ExternalAssetFetcher
import com.chevstrap.rbx.models.entities.UserDetails
import com.chevstrap.rbx.utility.Thumbnails
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.time.Duration.Companion.milliseconds

class RobloxDiscordRPC(private val activityWatcher: ActivityWatcher) {
    private val externalAssetFetcher =
        ExternalAssetFetcher()

    private var discordRichPresence: DiscordWsManager? =
        DiscordWsManager()

    private val presenceScope =
        CoroutineScope(
            SupervisorJob() + Dispatchers.IO
        )

    private val visibilityScope =
        CoroutineScope(
            SupervisorJob() + Dispatchers.IO
        )

    private var presenceJob: Job? = null
    private var visibilityJob: Job? = null

    private var isDisposed = false
    private var isVisible = true
    private var discordReady = false

    private var currentPresence: String? = null
    private var currentGameStartTime: Long? = null
    private var customMessageRPC: JSONObject? = null

    private val rpcSpamLock = Any()

    private val thumbnailCache:
            ConcurrentMap<String, String> =
        ConcurrentHashMap()

    private val lastRPCSignatures =
        HashMap<String, String>()

    private val lastRPCMessageTimes =
        HashMap<String, Long>()

    private val jsonFormatter =
        Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

    companion object {
        const val APPLICATION_ID =
            "1530730297967120534"

        const val RPC_MIN_INTERVAL_MS =
            1000L

        private const val MAX_THUMBNAIL_CACHE_SIZE =
            100
    }

    data class PresenceImages(
        val large: String? = null,
        val small: String? = null,
        val smallHoverText: String? = null
    )

    private data class CustomPresence(
        val details: String? = null,
        val state: String? = null,
        val smallImage: String? = null,
        val smallText: String? = null,
        val largeImage: String? = null
    )

    private data class ButtonResult(
        val buttons: Buttons? = null,
        val metadata: Metadata? = null
    )

    fun isVisibleState(): Boolean =
        isVisible

    private fun registerListener() {
        discordRichPresence?.setListener(
            object :
                DiscordWsImplementation.DiscordWsListener {

                override fun onReady(
                    user: JSONObject?
                ) {
                    discordReady = true

                    App.logger.writeLine(
                        "RobloxDiscordRPC::onReady",
                        "Received ready from user ${user?.optString("username")}"
                    )

                    updatePresence()
                }

                override fun onPresenceUpdate() {
                }

                override fun onError(
                    message: String,
                    throwable: Throwable?
                ) {
                    App.logger.writeLine(
                        "RobloxDiscordRPC::onError",
                        "An RPC error occurred - $message"
                    )

                    throwable?.let {
                        App.logger.writeException(
                            "Discord RPC error",
                            it as Exception?
                        )
                    }
                }

                override fun onConnectionEstablished() {
                    discordReady = false
                }

                override fun onConnectionFailed() {
                    discordReady = false
                }

                override fun onClose(
                    reason: String,
                    code: Int
                ) {
                    discordReady = false
                }
            }
        )
    }

    init {
        discordRichPresence?.setSession(
            PrefsManager.getToken(),
            PrefsManager.getStatus()
        )

        registerListener()
    }

    fun setCurrentGame() {
        if (
            isDisposed ||
            !App.config.data.showGameActivity
        ) {
            return
        }

        if (!activityWatcher.isInExperienceState()) {
            customMessageRPC = null
            currentPresence = null
            currentGameStartTime = null
            updatePresence()
            return
        }

        presenceJob?.cancel()

        presenceJob =
            presenceScope.launch {
                try {
                    ensureActive()

                    val activityData =
                        activityWatcher.getActivityData()
                            ?: return@launch

                    val rpcData =
                        customMessageRPC

                    val custom =
                        parseCustomPresence(rpcData)

                    val detailsData =
                        activityData.universeDetails?.data

                    val gameName =
                        detailsData?.name.orEmpty()

                    val details =
                        custom?.details
                            ?: gameName.ifBlank {
                                "Playing Roblox"
                            }

                    val icons =
                        updatePresenceIconsAsync(
                            activityData,
                            rpcData
                        )

                    ensureActive()

                    val smallImage: String?
                    val smallText: String?

                    if (
                        App.config.data.showRobloxAccount
                    ) {
                        val userDetails =
                            UserDetails.fetch(
                                activityData.userId
                            )

                        val displayName =
                            userDetails.data?.displayName

                        val userName =
                            userDetails.data?.name

                        smallImage =
                            icons.small

                        smallText =
                            if (
                                displayName.isNullOrBlank() ||
                                displayName == userName
                            ) {
                                "Playing on @$userName"
                            } else {
                                "Playing on $displayName @$userName"
                            }
                    } else {
                        smallImage =
                            custom?.smallImage
                                ?: icons.small

                        smallText =
                            custom?.smallText
                                ?: icons.smallHoverText.orEmpty()
                    }

                    val assets =
                        Assets(
                            largeImage =
                                custom?.largeImage
                                    ?: icons.large,
                            smallImage =
                                smallImage,
                            smallText =
                                smallText
                        )

                    val creatorName =
                        detailsData?.creator?.name
                            ?.takeIf {
                                it.isNotBlank()
                            }
                            ?: "N/A"

                    val verifiedSuffix =
                        if (
                            detailsData?.creator
                                ?.hasVerifiedBadge == true
                        ) {
                            " ☑️"
                        } else {
                            ""
                        }

                    val reserved =
                        activityData.serverType ==
                                ServerType.RESERVED &&
                                activityData.rpcLaunchData
                                    .isNullOrEmpty()

                    val state =
                        custom?.state
                            ?: if (reserved) {
                                "In a Reserved server"
                            } else {
                                "By $creatorName$verifiedSuffix"
                            }

                    if (currentGameStartTime == null) {
                        currentGameStartTime =
                            System.currentTimeMillis() / 1000L
                    }

                    val timestamps =
                        Timestamps(
                            start =
                                currentGameStartTime!! * 1000L
                        )

                    val buttons =
                        getButtonsInfo(
                            activityData
                        )

                    val presence =
                        Activity(
                            name = "Roblox",
                            type = 0,
                            applicationId =
                                APPLICATION_ID,
                            createdAt =
                                System.currentTimeMillis(),
                            details =
                                details,
                            state =
                                state,
                            timestamps =
                                timestamps,
                            assets =
                                assets,
                            buttons =
                                buttons.buttons?.items,
                            metadata =
                                buttons.metadata
                        )

                    ensureActive()

                    currentPresence =
                        jsonFormatter.encodeToString(
                            presence
                        )

                    App.logger.writeLine(
                        "RobloxDiscordRPC::setCurrentGame",
                        "Prepared Rich Presence: $currentPresence"
                    )

                    updatePresence()
                } catch (
                    e: CancellationException
                ) {
                    throw e
                } catch (
                    e: Exception
                ) {
                    App.logger.writeException(
                        "Failed to update Discord presence",
                        e
                    )
                }
            }
    }

    private fun parseCustomPresence(
        data: JSONObject?
    ): CustomPresence? {
        if (data == null) {
            return null
        }

        fun text(
            key: String
        ): String? {
            if (!data.has(key)) {
                return null
            }

            return data.optString(key)
                .takeIf {
                    it.isNotBlank() &&
                            it != "<reset>"
                }
        }

        fun image(
            key: String
        ): Pair<String?, String?> {
            val obj =
                data.optJSONObject(key)
                    ?: return null to null

            if (
                obj.optBoolean("reset") ||
                obj.optBoolean("clear")
            ) {
                return null to null
            }

            val assetId =
                obj.optString("assetId")
                    .takeIf {
                        it.isNotBlank() &&
                                it != "null"
                    }
                    ?.let(::resolveAsset)

            val hoverText =
                obj.optString("hoverText")
                    .takeIf {
                        it.isNotBlank()
                    }

            return assetId to hoverText
        }

        val small =
            image("smallImage")

        val large =
            image("largeImage")

        return CustomPresence(
            details =
                text("details"),
            state =
                text("state"),
            smallImage =
                small.first,
            smallText =
                small.second,
            largeImage =
                large.first
        )
    }

    private fun resolveAsset(
        assetId: String
    ): String {
        if (!assetId.startsWith("asset:")) {
            return assetId
        }

        return "https://assetgame.roblox.com/asset/?id=" +
                assetId.removePrefix("asset:")
    }

    private suspend fun updatePresenceIconsAsync(
        activityData: ActivityData,
        rpcData: JSONObject?
    ): PresenceImages {
        currentCoroutineContext().ensureActive()

        val gameThumbnail =
            Thumbnails.getThumbnailUrl(
                ThumbnailRequest(
                    targetId =
                        activityData.universeId,
                    type =
                        "GameIcon",
                    size =
                        "512x512",
                    isCircular =
                        false
                )
            )

        val profileThumbnail =
            if (
                App.config.data.showRobloxAccount
            ) {
                try {
                    val userDetails =
                        UserDetails.fetch(
                            activityData.userId
                        )

                    userDetails.thumbnail
                        ?.imageUrl
                        ?.let {
                            fetchCachedThumbnail(it)
                        }
                } catch (
                    e: CancellationException
                ) {
                    throw e
                } catch (
                    _: Exception
                ) {
                    null
                }
            } else {
                null
            }

        val large =
            rpcData
                ?.optJSONObject("largeImage")
                ?.optString("assetId")
                ?.takeIf {
                    it.isNotBlank() &&
                            it != "null"
                }
                ?.let(::resolveAsset)
                ?.let {
                    fetchCachedThumbnail(it)
                }

        val smallObject =
            rpcData
                ?.optJSONObject("smallImage")

        val small =
            smallObject
                ?.optString("assetId")
                ?.takeIf {
                    it.isNotBlank() &&
                            it != "null"
                }
                ?.let(::resolveAsset)
                ?.let {
                    fetchCachedThumbnail(it)
                }

        val smallText =
            smallObject
                ?.optString("hoverText")
                ?.takeIf {
                    it.isNotBlank()
                }

        val fallbackLarge =
            gameThumbnail?.let {
                fetchCachedThumbnail(it)
            }

        return PresenceImages(
            large =
                large ?: fallbackLarge,
            small =
                small ?: profileThumbnail,
            smallHoverText =
                smallText
        )
    }

    private fun getButtonsInfo(
        activityData: ActivityData
    ): ButtonResult {
        if (
            !App.config.data.allowActivityJoining
        ) {
            return ButtonResult()
        }

        val reserved =
            activityData.serverType ==
                    ServerType.RESERVED

        val hideJoin =
            reserved &&
                    activityData.rpcLaunchData
                        .isNullOrEmpty()

        val buttons =
            mutableListOf<String>()

        val urls =
            mutableListOf<String>()

        if (!hideJoin) {
            buttons += "Join Server"

            urls +=
                activityData.getInviteDeeplink("")
        }

        buttons += "See game page"

        urls +=
            "https://www.roblox.com/games/" +
                    activityData.rootPlaceId

        return ButtonResult(
            buttons =
                Buttons(buttons),
            metadata =
                Metadata(urls)
        )
    }

    fun processRPCMessage(
        message: JSONObject
    ) {
        if (isDisposed) {
            return
        }

        val command =
            message.optString("command")

        App.logger.writeLine(
            "RobloxDiscordRPC::processRPCMessage",
            "Received RPC command: '$command'"
        )

        when (command) {
            "SetRichPresence" ->
                processSetRichPresence(message)

            "SetLaunchData" ->
                processSetLaunchData(message)
        }
    }

    private fun processSetRichPresence(
        message: JSONObject
    ) {
        if (
            isDisposed ||
            !App.config.data.discordAllowCustomPresence
        ) {
            return
        }

        val activityData =
            activityWatcher.getActivityData()
                ?: return

        val creator =
            activityData.universeDetails
                ?.data
                ?.creator

        if (
            App.config.data
                .discordVerifiedCustomPresenceOnly &&
            creator?.hasVerifiedBadge != true
        ) {
            return
        }

        val data =
            message.optJSONObject("data")
                ?: return

        if (
            !shouldProcessRPCMessage(
                "SetRichPresence",
                data
            )
        ) {
            return
        }

        validateText(
            data,
            "details"
        )

        validateText(
            data,
            "state"
        )

        sanitizeImage(
            data,
            "smallImage"
        )

        sanitizeImage(
            data,
            "largeImage"
        )

        customMessageRPC =
            JSONObject(data.toString())

        App.logger.writeLine(
            "RobloxDiscordRPC::processSetRichPresence",
            "Custom Rich Presence: $customMessageRPC"
        )

        if (
            activityWatcher.isInExperienceState()
        ) {
            setCurrentGame()
        }
    }

    private fun validateText(
        data: JSONObject,
        key: String
    ) {
        if (!data.has(key)) {
            return
        }

        val value =
            data.optString(key)

        if (
            value.length > 128 ||
            value == "<reset>"
        ) {
            data.remove(key)
        }
    }

    private fun sanitizeImage(
        data: JSONObject,
        key: String
    ) {
        val image =
            data.optJSONObject(key)
                ?: return

        if (image.optBoolean("reset")) {
            data.remove(key)
            return
        }

        if (image.optBoolean("clear")) {
            data.remove(key)
        }
    }

    private fun processSetLaunchData(
        message: JSONObject
    ) {
        if (isDisposed) {
            return
        }

        val data =
            message.optString("data")
                .takeIf {
                    it.isNotBlank()
                }
                ?: return

        if (data.length > 200) {
            return
        }

        activityWatcher.getActivityData()
            ?.rpcLaunchData = data

        if (
            activityWatcher.isInExperienceState()
        ) {
            setCurrentGame()
        }
    }

    private suspend fun fetchCachedThumbnail(
        url: String
    ): String? {
        thumbnailCache[url]?.let {
            return it
        }

        currentCoroutineContext().ensureActive()

        val result =
            externalAssetFetcher.fetchMPOfUrl(
                APPLICATION_ID,
                url
            )

        if (!result.isNullOrBlank()) {
            addToThumbnailCache(
                url,
                result
            )
        }

        return result
    }

    private fun shouldProcessRPCMessage(
        command: String,
        data: JSONObject
    ): Boolean {
        synchronized(rpcSpamLock) {
            val now =
                System.currentTimeMillis()

            val signature =
                data.toString()

            if (
                lastRPCSignatures[command] ==
                signature
            ) {
                return false
            }

            val last =
                lastRPCMessageTimes[command]
                    ?: 0L

            if (
                now - last <
                RPC_MIN_INTERVAL_MS
            ) {
                return false
            }

            lastRPCSignatures[command] =
                signature

            lastRPCMessageTimes[command] =
                now

            return true
        }
    }

    fun setVisibility(
        visible: Boolean
    ) {
        if (isDisposed) {
            return
        }

        visibilityJob?.cancel()

        visibilityJob =
            visibilityScope.launch {
                delay(1000.milliseconds)

                if (isDisposed) {
                    return@launch
                }

                isVisible =
                    visible

                if (visible) {
                    setCurrentGame()
                } else {
                    clearRPC()
                }
            }
    }

    private fun updatePresence() {
        if (
            isDisposed ||
            !isVisible
        ) {
            return
        }

        if (
            currentPresence.isNullOrEmpty() ||
            !activityWatcher.isInExperienceState()
        ) {
            clearRPC()
            return
        }

        val presence =
            currentPresence
                ?: return

        App.logger.writeLine(
            "RobloxDiscordRPC::updatePresence",
            "Sending Rich Presence: $presence"
        )

        discordRichPresence?.setRPC(
            presence
        )
    }

    private fun clearRPC() {
        currentPresence = null

        if (discordReady) {
            discordRichPresence?.clearRPC()
        }
    }

    private fun addToThumbnailCache(
        key: String,
        value: String
    ) {
        synchronized(thumbnailCache) {
            if (
                thumbnailCache.size >=
                MAX_THUMBNAIL_CACHE_SIZE
            ) {
                thumbnailCache.keys
                    .firstOrNull()
                    ?.let {
                        if (it != key) {
                            thumbnailCache.remove(it)
                        }
                    }
            }

            thumbnailCache[key] =
                value
        }
    }

    fun dispose() {
        if (isDisposed) {
            return
        }

        isDisposed = true
        isVisible = false
        discordReady = false

        currentPresence = null
        currentGameStartTime = null
        customMessageRPC = null

        presenceJob?.cancel()
        visibilityJob?.cancel()

        presenceJob = null
        visibilityJob = null

        thumbnailCache.clear()

        discordRichPresence?.stopRPC()
        discordRichPresence = null

        presenceScope.cancel()
        visibilityScope.cancel()
    }
}
