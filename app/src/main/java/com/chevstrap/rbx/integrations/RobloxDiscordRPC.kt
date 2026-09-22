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
    private val externalAssetFetcher = ExternalAssetFetcher()
    private var discordRichPresence: DiscordWsManager? = null
    private val presenceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val visibilityScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var visibilityJob: Job? = null
    private var presenceJob: Job? = null
    private var isDisposed = false
    private var isVisible = true
    private var currentPresence: String? = null
    private var currentGameStartTime: Long? = null

    private val rpcSpamLock = Any()
    private val thumbnailCache: ConcurrentMap<String, String> = ConcurrentHashMap()
    private val lastRPCSignatures = HashMap<String, String>()
    private val lastRPCMessageTimes = HashMap<String, Long>()

    private val jsonFormatter = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    init {
        discordRichPresence = DiscordWsManager()
        discordRichPresence?.setSession(PrefsManager.getToken(), PrefsManager.getStatus())
        registerListener()
    }

    companion object {
        const val APPLICATION_ID = "1530730297967120534"
        const val RPC_MIN_INTERVAL_MS = 1000L
        private const val MAX_THUMBNAIL_CACHE_SIZE = 100
    }

    data class PresenceImages(
        val large: String? = null,
        val small: String? = null,
        val smallHoverText: String? = null
    )

    fun isVisibleState(): Boolean {
        return isVisible
    }

    fun registerListener() {
        if (isDisposed) return

        discordRichPresence?.setListener(
            object : DiscordWsImplementation.DiscordWsListener {
                override fun onReady(user: JSONObject?) {
                    App.logger.writeLine(
                        "RobloxDiscordRPC::onReady",
                        "Received ready from user ${user?.optString("username")}"
                    )
                }

                override fun onPresenceUpdate() {
                    App.logger.writeLine(
                        "RobloxDiscordRPC::onPresenceUpdate",
                        "Presence updated"
                    )
                }

                override fun onError(message: String, throwable: Throwable?) {
                    App.logger.writeLine(
                        "RobloxDiscordRPC::onError",
                        "An RPC error occurred - $message"
                    )
                    throwable?.let {
                        App.logger.writeException("Discord RPC error", it as Exception?)
                    }
                }

                override fun onConnectionEstablished() {
                    App.logger.writeLine(
                        "RobloxDiscordRPC::onConnectionEstablished",
                        "Established connection with Discord RPC"
                    )
                }

                override fun onConnectionFailed() {
                    App.logger.writeLine(
                        "RobloxDiscordRPC::onConnectionFailed",
                        "Failed to establish connection with Discord RPC"
                    )
                }

                override fun onClose(reason: String, code: Int) {
                    App.logger.writeLine(
                        "RobloxDiscordRPC::onClose",
                        "Lost connection to Discord RPC - $reason ($code)"
                    )
                }
            }
        )
    }

    fun setCurrentGame() {
        if (isDisposed || !App.config.data.showGameActivity) return

        if (!activityWatcher.isInExperienceState()) {
            currentPresence = ""
            currentGameStartTime = null
            updatePresence()
            return
        }

        if (currentGameStartTime == null) {
            currentGameStartTime = System.currentTimeMillis()
        }

        presenceJob?.cancel()
        presenceJob = presenceScope.launch {
            val logIdentifier = "RobloxDiscordRPC::setCurrentGame"

            try {
                ensureActive()
                val activityData = activityWatcher.getActivityData() ?: return@launch
                val rpcData = activityData.customMessageRPC
                val detailsData = activityData.universeDetails?.data
                val gameName = detailsData?.name.orEmpty()

                val rpcDetails = rpcData?.optString("details")?.takeIf { it.isNotBlank() }
                val baseDetails = rpcDetails ?: gameName.ifBlank { "Playing Roblox" }

                val icons = updatePresenceIconsAsync(activityData, rpcData)
                ensureActive()

                val smallImg: String?
                val smallTxt: String?

                if (App.config.data.showRobloxAccount) {
                    val userDetails = UserDetails.fetch(activityData.userId)
                    val displayName = userDetails.data?.displayName
                    val userName = userDetails.data?.name

                    smallImg = icons.small
                    smallTxt = if (displayName.isNullOrBlank() || displayName == userName) {
                        "Playing on @$userName"
                    } else {
                        "Playing on $displayName @$userName"
                    }
                } else {
                    smallImg = icons.small
                    smallTxt = icons.smallHoverText.orEmpty()
                }

                val assets = Assets(
                    largeImage = icons.large,
                    smallImage = smallImg,
                    smallText = smallTxt
                )

                ensureActive()
                val buttonInfo = getButtonsInfo(activityData)
                val rpcState = rpcData?.optString("state")?.takeIf { it.isNotBlank() }

                val isShowReservedServer =
                    activityData.serverType == ServerType.RESERVED &&
                            activityData.rpcLaunchData.isNullOrEmpty()

                val creatorName = detailsData?.creator?.name
                    ?.takeIf { it.isNotBlank() } ?: "N/A"

                val verifiedSuffix = if (detailsData?.creator?.hasVerifiedBadge ?: false) " ☑️" else ""
                val state = when {
                    isShowReservedServer -> "In a Reserved server"
                    rpcState.isNullOrBlank() -> "By $creatorName$verifiedSuffix"
                    else -> rpcState
                }

                val startTime = currentGameStartTime ?: System.currentTimeMillis()
                val presence = Activity(
                    name = "Roblox",
                    type = 0,
                    applicationId = APPLICATION_ID,
                    createdAt = startTime,
                    details = baseDetails,
                    state = state,
                    timestamps = Timestamps(start = startTime),
                    assets = assets,
                    buttons = buttonInfo.buttons?.items,
                    metadata = buttonInfo.metadata
                )

                ensureActive()
                currentPresence = jsonFormatter.encodeToString(presence)
                updatePresence()

                App.logger.writeLine(
                    logIdentifier,
                    "Presence payload written and Discord service started."
                )
            } catch (e: CancellationException) {
                App.logger.writeLine(
                    logIdentifier,
                    "Previous presence update cancelled."
                )
                throw e
            } catch (e: Exception) {
                App.logger.writeException("Failed to update Discord presence", e)
            }
        }
    }

    private suspend fun updatePresenceIconsAsync(
        activityData: ActivityData,
        rpcData: JSONObject?
    ): PresenceImages {
        val logIdentifier = "RobloxDiscordRPC::UpdatePresenceIconsAsync"
        currentCoroutineContext().ensureActive()

        val gameThumbnailUrl = Thumbnails.getThumbnailUrl(
            ThumbnailRequest(
                targetId = activityData.universeId,
                type = "GameIcon",
                size = "512x512",
                isCircular = false
            )
        )

        val profileThumbnailUrl = if (App.config.data.showRobloxAccount) {
            try {
                currentCoroutineContext().ensureActive()
                val userDetails = UserDetails.fetch(activityData.userId)
                userDetails.thumbnail?.imageUrl?.let { fetchCachedThumbnail(it) }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                App.logger.writeLine(
                    logIdentifier,
                    "Failed to fetch Roblox profile thumbnail"
                )
                null
            }
        } else {
            null
        }

        currentCoroutineContext().ensureActive()
        val largeImageData = rpcData?.optJSONObject("largeImage")
        val smallImageData = rpcData?.optJSONObject("smallImage")

        val largeAssetId = largeImageData?.optString("assetId")?.takeIf {
            it.isNotBlank() && it != "null"
        }

        val smallAssetId = smallImageData?.optString("assetId")?.takeIf {
            it.isNotBlank() && it != "null"
        }

        val smallHoverText = smallImageData?.optString("hoverText")?.takeIf {
            it.isNotBlank()
        }

        fun resolveAssetUrl(assetId: String?): String? = assetId?.let {
            if (it.startsWith("asset:")) {
                val id = it.removePrefix("asset:")
                "https://assetgame.roblox.com/asset/?id=$id"
            } else {
                it
            }
        }

        val rpcLargeImageUrl = resolveAssetUrl(largeAssetId)
        val rpcSmallImageUrl = resolveAssetUrl(smallAssetId)

        currentCoroutineContext().ensureActive()
        val rpcLargeImage = rpcLargeImageUrl?.let { fetchCachedThumbnail(it) }

        currentCoroutineContext().ensureActive()
        val rpcSmallImage = rpcSmallImageUrl?.let { fetchCachedThumbnail(it) }

        currentCoroutineContext().ensureActive()
        val fallbackLargeImage = gameThumbnailUrl?.let { fetchCachedThumbnail(it) }

        return PresenceImages(
            large = rpcLargeImage ?: fallbackLargeImage,
            small = rpcSmallImage ?: profileThumbnailUrl,
            smallHoverText = smallHoverText
        )
    }

    private data class ButtonResult(
        val buttons: Buttons? = null,
        val metadata: Metadata? = null
    )

    private fun getButtonsInfo(activityData: ActivityData): ButtonResult {
        if (!App.config.data.allowActivityJoining) return ButtonResult()

        val rootPlaceId = activityData.rootPlaceId
        val rpcLaunchData = activityData.rpcLaunchData
        val isReservedServer = activityData.serverType == ServerType.RESERVED
        val gameUrl = "https://www.roblox.com/games/$rootPlaceId"

        val buttonsList = mutableListOf<String>()
        val urlsList = mutableListOf<String>()

        val shouldHideJoinButton =
            isReservedServer && rpcLaunchData.isNullOrEmpty()

        if (!shouldHideJoinButton) {
            buttonsList.add("Join Server")
            urlsList.add(activityData.getInviteDeeplink(""))
        }

        buttonsList.add("See game page")
        urlsList.add(gameUrl)

        if (buttonsList.isEmpty()) return ButtonResult()
        return ButtonResult(
            buttons = Buttons(items = buttonsList),
            metadata = Metadata(buttonUrls = urlsList)
        )
    }

    fun processRPCMessage(message: JSONObject) {
        if (isDisposed) return

        val logIdentifier = "RobloxDiscordRPC::processRPCMessage"
        val command = message.optString("command").takeIf { it.isNotBlank() }

        if (command == null) {
            App.logger.writeLine(
                logIdentifier,
                "Failed to parse message! (Command is empty)"
            )
            return
        }

        App.logger.writeLine(
            logIdentifier,
            "Received RPC command: '$command'"
        )

        when (command) {
            "SetRichPresence" -> processSetRichPresence(message)
            "SetLaunchData" -> {
                val data = message.optString("data").takeIf { it.isNotBlank() }

                if (data == null) {
                    App.logger.writeLine(
                        logIdentifier,
                        "SetLaunchData data is not a string"
                    )
                    return
                }

                if (data.length > 200) {
                    App.logger.writeLine(
                        logIdentifier,
                        "Data cannot be longer than 200 characters"
                    )
                    return
                }

                activityWatcher.getActivityData()?.rpcLaunchData = data
                App.logger.writeLine(
                    logIdentifier,
                    "Received SetLaunchData: '$data'"
                )
            }

            else -> {
                App.logger.writeLine(
                    logIdentifier,
                    "Unknown RPC command: '$command'"
                )
            }
        }
    }

    private fun processSetRichPresence(message: JSONObject) {
        if (isDisposed || !App.config.data.discordAllowCustomPresence) {
            return
        }
        val detailsData = activityWatcher.getActivityData()?.universeDetails?.data
        if (
            App.config.data.discordVerifiedCustomPresenceOnly &&
            detailsData?.creator?.hasVerifiedBadge != true
        ) {
            return
        }

        val log = "RobloxDiscordRPC::ProcessSetRichPresence"
        val data = message.optJSONObject("data") ?: run {
            App.logger.writeLine(log, "Failed to parse message! (Data is null)")
            return
        }

        if (!shouldProcessRPCMessage("SetRichPresence", data)) return
        fun validateText(key: String) {
            if (!data.has(key)) return

            val value = data.optString(key)
            when {
                value.length > 128 -> {
                    App.logger.writeLine(
                        log,
                        "$key cannot be longer than 128 characters"
                    )
                }

                value == "<reset>" -> {
                    data.remove(key)
                    App.logger.writeLine(
                        log,
                        "Reset $key"
                    )
                }
            }
        }

        validateText("details")
        validateText("state")

        listOf("smallImage", "largeImage").forEach { key ->
            val image = data.optJSONObject(key) ?: return@forEach

            when {
                image.optBoolean("reset") -> {
                    data.remove(key)

                    App.logger.writeLine(
                        log,
                        "Reset $key"
                    )
                }

                image.optBoolean("clear") -> {
                    image.remove("assetId")
                    image.remove("hoverText")

                    App.logger.writeLine(
                        log,
                        "Cleared $key"
                    )
                }

                image.has("assetId") &&
                        image.optString("assetId").isBlank() -> {
                    image.remove("assetId")

                    App.logger.writeLine(
                        log,
                        "Removed empty $key assetId"
                    )
                }
            }
        }

        activityWatcher.getActivityData()?.customMessageRPC = data
        App.logger.writeLine(
            log,
            "Received SetRichPresence data: '$data'"
        )
        setCurrentGame()
    }

    private suspend fun fetchCachedThumbnail(url: String): String? {
        getFromThumbnailCache(url)?.let { return it }

        currentCoroutineContext().ensureActive()
        val result = externalAssetFetcher.fetchMPOfUrl(APPLICATION_ID, url)

        if (!result.isNullOrBlank()) {
            addToThumbnailCache(url, result)
        }

        return result
    }

    private fun shouldProcessRPCMessage(
        command: String,
        data: JSONObject
    ): Boolean {
        synchronized(rpcSpamLock) {
            val logIdentifier = "RobloxDiscordRPC::RPCAntiSpam"
            val currentTime = System.currentTimeMillis()
            val dataString = data.toString()

            if (lastRPCSignatures[command] == dataString) {
                App.logger.writeLine(
                    logIdentifier,
                    "Ignoring duplicate RPC message: '$dataString'"
                )
                return false
            }

            val lastTime = lastRPCMessageTimes[command] ?: 0L
            if (currentTime - lastTime < RPC_MIN_INTERVAL_MS) {
                App.logger.writeLine(
                    logIdentifier,
                    "Ignoring RPC message due to rate limit: '$dataString'"
                )
                return false
            }

            lastRPCSignatures[command] = dataString
            lastRPCMessageTimes[command] = currentTime
            return true
        }
    }

    fun setVisibility(visible: Boolean) {
        if (isDisposed) return

        visibilityJob?.cancel()
        visibilityJob = visibilityScope.launch {
            delay(1000.milliseconds)

            if (isDisposed) return@launch
            isVisible = visible
            if (visible) {
                setCurrentGame()
            } else {
                clearRPC()
            }
        }
    }

    private fun updatePresence() {
        if (isDisposed || !isVisible) return
        val logIdentifier = "DiscordRichPresence::updatePresence"

        if (currentPresence.isNullOrEmpty() || !activityWatcher.isInExperienceState()) {
            App.logger.writeLine(
                logIdentifier,
                "Presence is empty or game leaving, clearing"
            )
            clearRPC()
            return
        }

        App.logger.writeLine(
            logIdentifier,
            "Updating presence"
        )
        discordRichPresence?.setRPC(currentPresence!!)
    }

    private fun clearRPC() {
        currentPresence = null
        discordRichPresence?.clearRPC()
    }

    private fun addToThumbnailCache(key: String, value: String) {
        synchronized(thumbnailCache) {
            if (thumbnailCache.size >= MAX_THUMBNAIL_CACHE_SIZE) {
                val firstKey = thumbnailCache.keys.firstOrNull()

                if (firstKey != null && firstKey != key) {
                    thumbnailCache.remove(firstKey)
                }
            }
            thumbnailCache[key] = value
        }
    }

    private fun getFromThumbnailCache(key: String): String? = thumbnailCache[key]

    fun dispose() {
        if (isDisposed) return

        App.logger.writeLine(
            "DiscordRichPresence::dispose",
            "Cleaning up Discord RPC and Presence"
        )

        isDisposed = true
        isVisible = false

        currentGameStartTime = null
        currentPresence = null

        visibilityJob?.cancel()
        presenceJob?.cancel()

        visibilityJob = null
        presenceJob = null

        thumbnailCache.clear()

        discordRichPresence?.stopRPC()
        discordRichPresence = null

        visibilityScope.cancel()
        presenceScope.cancel()
    }
}