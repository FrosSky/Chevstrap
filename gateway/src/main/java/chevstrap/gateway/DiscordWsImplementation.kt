package chevstrap.gateway

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds

class DiscordWsImplementation {

    private var heartbeat = 0
    private var sequence: Int? = null
    private var sessionId = ""
    private var resumeGatewayUrl: String? = null
    private var reconnectDelay = INITIAL_RECONNECT_DELAY

    @Volatile
    private var userInfoCallback: ((JSONObject?) -> Unit)? = null

    @Volatile
    private var userInfo: JSONObject? = null

    @Volatile
    private var webSocket: WebSocket? = null

    @Volatile
    private var heartbeatExecutor: ScheduledExecutorService? = null

    @Volatile
    private var heartbeatFuture: ScheduledFuture<*>? = null

    @Volatile
    private var heartbeatWatchdog: ScheduledFuture<*>? = null

    @Volatile
    private var reconnectThread: Thread? = null

    @Volatile
    private var invalidSessionJob: Job? = null

    @Volatile
    private var client: OkHttpClient? = null

    @Volatile
    private var isStopping = false

    @Volatile
    private var isConnecting = false

    @Volatile
    private var isReady = false

    @Volatile
    private var reconnectScheduled = false

    @Volatile
    private var reconnectRequested = false

    @Volatile
    private var heartbeatAwaitingAck = false

    @Volatile
    private var lastHeartbeatSentAt = 0L

    @Volatile
    private var shutdownFinished = true

    @Volatile
    private var connectionMode = ConnectionMode.RPC

    @Volatile
    private var presencePending = false

    @Volatile
    private var pendingActivityRPC: String? = null

    @Volatile
    private var _token: String? = null

    @Volatile
    private var _status: String? = null

    @Volatile
    var isRunning = false
        private set

    private val scope =
        CoroutineScope(
            SupervisorJob() + Dispatchers.Default
        )

    interface DiscordWsListener {
        fun onReady(user: JSONObject?)
        fun onPresenceUpdate()
        fun onError(message: String, throwable: Throwable? = null)
        fun onConnectionEstablished()
        fun onConnectionFailed()
        fun onClose(reason: String, code: Int)
    }

    private var listener: DiscordWsListener? = null

    private val connectionLock = Any()

    private enum class ConnectionMode {
        RPC,
        USER_INFO
    }

    var token: String?
        get() = _token
        set(value) {
            _token = value
        }

    var status: String?
        get() = _status
        set(value) {
            _status = value
        }

    fun setListener(listener: DiscordWsListener?) {
        this.listener = listener
    }

    fun getUserInfo(): JSONObject? =
        userInfo?.let {
            JSONObject(it.toString())
        }

    @Synchronized
    fun start() {
        connectionMode = ConnectionMode.RPC
        startConnection()
    }

    @Synchronized
    fun startUserInfo(callback: (JSONObject?) -> Unit) {
        userInfoCallback = callback
        connectionMode = ConnectionMode.USER_INFO
        startConnection()
    }

    @Synchronized
    fun stop() {
        stopConnection()
    }

    fun clearPresence() {
        if (connectionMode != ConnectionMode.RPC) {
            return
        }

        scope.launch {
            waitUntilReady()

            if (
                !isRunning ||
                isStopping ||
                !isReady
            ) {
                return@launch
            }

            sendClearPresenceNow()
        }
    }

    fun setPresence(activityRPC: String) {
        if (connectionMode != ConnectionMode.RPC) {
            return
        }

        if (activityRPC.isBlank()) {
            return
        }

        synchronized(connectionLock) {
            if (isStopping) {
                return
            }

            pendingActivityRPC = activityRPC
            presencePending = true

            if (!isRunning) {
                startConnection()
                return
            }
        }

        scope.launch {
            waitUntilReady()

            if (
                !isRunning ||
                isStopping ||
                !isReady
            ) {
                return@launch
            }

            sendPendingPresence()
        }
    }

    private fun startConnection() {
        synchronized(connectionLock) {
            if (
                isRunning &&
                !isStopping
            ) {
                return
            }

            shutdownFinished = false

            isStopping = false
            isRunning = true
            isReady = false
            isConnecting = false
            reconnectScheduled = false
            reconnectRequested = false

            sequence = null
            sessionId = ""
            resumeGatewayUrl = null
            reconnectDelay = INITIAL_RECONNECT_DELAY

            heartbeat = 0
            heartbeatAwaitingAck = false
            lastHeartbeatSentAt = 0L

            if (connectionMode == ConnectionMode.RPC) {
                userInfo = null
            }
        }

        stopHeartbeat()

        reconnectThread?.interrupt()
        reconnectThread = null

        invalidSessionJob?.cancel()
        invalidSessionJob = null

        client?.let {
            runCatching {
                it.dispatcher.cancelAll()
                it.connectionPool.evictAll()
                it.cache?.close()
            }
        }

        client =
            OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .build()

        connectWebSocket()
    }

    private fun stopConnection() {
        val socket: WebSocket?

        synchronized(connectionLock) {
            if (
                shutdownFinished &&
                !isRunning &&
                webSocket == null
            ) {
                return
            }

            isStopping = true
            isRunning = false
            isReady = false
            isConnecting = false
            reconnectRequested = false
            reconnectScheduled = false

            presencePending = false
            pendingActivityRPC = null

            socket = webSocket
            webSocket = null
        }

        stopHeartbeat()

        reconnectThread?.interrupt()
        reconnectThread = null

        invalidSessionJob?.cancel()
        invalidSessionJob = null

        if (
            connectionMode == ConnectionMode.RPC &&
            socket != null
        ) {
            sendClearPresence(socket)
        }

        socket?.close(
            1000,
            "Connection to gateway closed"
        )

        client?.let {
            runCatching {
                it.dispatcher.cancelAll()
                it.connectionPool.evictAll()
                it.cache?.close()
            }
        }

        client = null

        synchronized(connectionLock) {
            shutdownFinished = true
        }
    }

    private suspend fun waitUntilReady() {
        while (
            currentCoroutineContext().isActive &&
            isRunning &&
            !isStopping &&
            !isReady
        ) {
            delay(10.milliseconds)
        }
    }

    private fun sendPendingPresence() {
        val activityRPC =
            synchronized(connectionLock) {
                if (
                    !presencePending ||
                    isStopping ||
                    !isRunning ||
                    !isReady
                ) {
                    return
                }

                presencePending = false

                pendingActivityRPC.also {
                    pendingActivityRPC = null
                }
            }

        if (activityRPC.isNullOrBlank()) {
            return
        }

        sendPresenceNow(activityRPC)
    }

    private fun sendPresenceNow(activityRPC: String) {
        if (connectionMode != ConnectionMode.RPC) {
            return
        }

        if (
            activityRPC.isBlank() ||
            isStopping ||
            !isRunning ||
            !isReady
        ) {
            return
        }

        val socket =
            synchronized(connectionLock) {
                webSocket
            } ?: return

        runCatching {
            JSONObject()
                .apply {
                    put(
                        "since",
                        System.currentTimeMillis()
                    )

                    put(
                        "activities",
                        JSONArray()
                            .put(
                                JSONObject(activityRPC)
                            )
                    )

                    put(
                        "status",
                        status
                    )

                    put(
                        "afk",
                        false
                    )
                }
                .let {
                    JSONObject()
                        .put("op", 3)
                        .put("d", it)
                }
                .toString()
                .let(socket::send)
        }
            .onSuccess { sent ->
                if (sent) {
                    listener?.onPresenceUpdate()
                }
            }
            .onFailure {
                listener?.onError(
                    it.message
                        ?: "Unknown WebSocket error",
                    it
                )
            }
    }

    private fun sendClearPresence(
        socket: WebSocket
    ): Boolean {
        if (connectionMode != ConnectionMode.RPC) {
            return false
        }

        return runCatching {
            val payload =
                JSONObject()
                    .put("op", 3)
                    .put(
                        "d",
                        JSONObject()
                            .put(
                                "since",
                                JSONObject.NULL
                            )
                            .put(
                                "activities",
                                JSONArray()
                            )
                            .put(
                                "status",
                                status
                            )
                            .put(
                                "afk",
                                false
                            )
                    )
                    .toString()

            socket.send(payload)
        }
            .onSuccess { sent ->
                if (sent) {
                    listener?.onPresenceUpdate()
                }
            }
            .onFailure { error ->
                listener?.onError(
                    error.message
                        ?: "Unknown WebSocket error",
                    error
                )
            }
            .getOrDefault(false)
    }

    private fun sendClearPresenceNow(): Boolean {
        val socket =
            synchronized(connectionLock) {
                if (
                    isStopping ||
                    !isRunning ||
                    !isReady
                ) {
                    return false
                }

                webSocket
            } ?: return false

        return sendClearPresence(socket)
    }

    private fun connectWebSocket() {
        synchronized(connectionLock) {
            if (
                isStopping ||
                !isRunning ||
                webSocket != null ||
                isConnecting ||
                reconnectScheduled
            ) {
                return
            }

            isConnecting = true
        }

        val currentClient =
            client ?: run {
                synchronized(connectionLock) {
                    isConnecting = false
                }

                scheduleReconnect()
                return
            }

        val request =
            Request.Builder()
                .url(
                    resumeGatewayUrl
                        ?: DEFAULT_GATEWAY_URL
                )
                .build()

        runCatching {
            currentClient.newWebSocket(
                request,
                Listener()
            )
        }.onFailure {
            synchronized(connectionLock) {
                isConnecting = false
            }

            listener?.onError(
                it.message
                    ?: "Unknown WebSocket error",
                it
            )

            scheduleReconnect()
        }
    }

    private fun scheduleReconnect() {
        synchronized(connectionLock) {
            if (
                isStopping ||
                !isRunning ||
                webSocket != null ||
                isConnecting ||
                reconnectScheduled
            ) {
                return
            }

            reconnectScheduled = true
        }

        val delay = reconnectDelay

        reconnectDelay =
            minOf(
                reconnectDelay * 2,
                MAX_RECONNECT_DELAY
            )

        reconnectThread?.interrupt()

        reconnectThread =
            Thread {
                try {
                    Thread.sleep(delay)
                } catch (_: InterruptedException) {
                    synchronized(connectionLock) {
                        reconnectScheduled = false
                    }

                    return@Thread
                }

                synchronized(connectionLock) {
                    reconnectScheduled = false
                }

                if (
                    !isStopping &&
                    isRunning
                ) {
                    connectWebSocket()
                }
            }.apply {
                name = "DiscordWebSocket-Reconnect"
                isDaemon = true
                start()
            }
    }

    private fun startHeartbeat() {
        if (
            isStopping ||
            !isRunning ||
            heartbeat <= 0
        ) {
            return
        }

        stopHeartbeat()

        val interval = heartbeat.toLong()

        heartbeatExecutor =
            Executors.newSingleThreadScheduledExecutor {
                Thread(
                    it,
                    "DiscordWebSocket-Heartbeat"
                ).apply {
                    isDaemon = true
                }
            }

        val initialDelay =
            ThreadLocalRandom.current()
                .nextLong(interval)

        heartbeatExecutor?.let {
            heartbeatFuture =
                it.scheduleWithFixedDelay(
                    ::sendHeartbeat,
                    initialDelay,
                    interval,
                    TimeUnit.MILLISECONDS
                )

            heartbeatWatchdog =
                it.scheduleWithFixedDelay(
                    ::checkHeartbeat,
                    interval,
                    interval,
                    TimeUnit.MILLISECONDS
                )
        }
    }

    private fun stopHeartbeat() {
        heartbeatFuture?.cancel(true)
        heartbeatWatchdog?.cancel(true)
        heartbeatExecutor?.shutdownNow()

        heartbeatFuture = null
        heartbeatWatchdog = null
        heartbeatExecutor = null

        heartbeatAwaitingAck = false
        lastHeartbeatSentAt = 0L
    }

    private fun sendHeartbeat(): Boolean {
        if (
            isStopping ||
            !isRunning
        ) {
            return false
        }

        val socket =
            synchronized(connectionLock) {
                webSocket
            } ?: return false

        if (
            heartbeatAwaitingAck &&
            System.currentTimeMillis() -
            lastHeartbeatSentAt >
            heartbeat * 2L
        ) {
            socket.cancel()
            return false
        }

        val sent =
            runCatching {
                socket.send(
                    JSONObject()
                        .put("op", 1)
                        .put(
                            "d",
                            sequence
                                ?: JSONObject.NULL
                        )
                        .toString()
                )
            }.getOrElse {
                listener?.onError(
                    it.message
                        ?: "Unknown WebSocket error",
                    it
                )

                false
            }

        if (sent) {
            heartbeatAwaitingAck = true

            lastHeartbeatSentAt =
                System.currentTimeMillis()
        } else {
            socket.cancel()
        }

        return sent
    }

    private fun checkHeartbeat() {
        if (
            !isStopping &&
            isRunning &&
            heartbeatAwaitingAck &&
            System.currentTimeMillis() -
            lastHeartbeatSentAt >
            heartbeat * 2L
        ) {
            webSocket?.cancel()
        }
    }

    private fun isCurrentSocket(
        socket: WebSocket
    ): Boolean {
        return synchronized(connectionLock) {
            webSocket === socket
        }
    }

    private inner class Listener :
        WebSocketListener() {

        override fun onOpen(
            webSocket: WebSocket,
            response: Response
        ) {
            synchronized(connectionLock) {
                isConnecting = false

                if (
                    isStopping ||
                    !isRunning ||
                    this@DiscordWsImplementation
                        .webSocket != null
                ) {
                    webSocket.cancel()
                    return
                }

                this@DiscordWsImplementation
                    .webSocket = webSocket
            }

            listener?.onConnectionEstablished()
        }

        override fun onMessage(
            webSocket: WebSocket,
            text: String
        ) {
            if (
                !isCurrentSocket(webSocket) ||
                isStopping ||
                !isRunning
            ) {
                return
            }

            try {
                val json =
                    JSONObject(text)

                val op =
                    json.getInt("op")

                if (
                    json.has("s") &&
                    !json.isNull("s")
                ) {
                    sequence =
                        json.getInt("s")
                }

                when (op) {
                    0 -> dispatch(json)

                    1 -> sendHeartbeat()

                    7 -> {
                        if (isStopping) {
                            return
                        }

                        reconnectRequested = true
                        isReady = false

                        stopHeartbeat()

                        runCatching {
                            webSocket.close(
                                4000,
                                "Discord requested reconnect"
                            )
                        }.onFailure {
                            webSocket.cancel()
                        }
                    }

                    9 -> invalidSession()

                    10 -> {
                        val interval =
                            json.optJSONObject("d")
                                ?.optLong(
                                    "heartbeat_interval",
                                    0L
                                )
                                ?: 0L

                        if (interval <= 0L) {
                            listener?.onError(
                                "Invalid Discord heartbeat interval"
                            )

                            webSocket.cancel()
                            return
                        }

                        heartbeat =
                            interval.toInt()

                        stopHeartbeat()

                        if (
                            isStopping ||
                            !isRunning
                        ) {
                            return
                        }

                        if (
                            sequence != null &&
                            sessionId.isNotBlank()
                        ) {
                            resume(webSocket)
                        } else {
                            identify(webSocket)
                        }

                        startHeartbeat()
                    }

                    11 -> {
                        heartbeatAwaitingAck = false
                    }
                }
            } catch (_: JSONException) {
            } catch (e: Exception) {
                listener?.onError(
                    e.message
                        ?: "Unknown WebSocket error",
                    e
                )
            }
        }

        private fun identify(
            socket: WebSocket
        ): Boolean {
            val currentToken =
                token?.takeIf {
                    it.isNotBlank()
                } ?: return false

            return runCatching {
                JSONObject()
                    .put("op", 2)
                    .put(
                        "d",
                        JSONObject()
                            .put(
                                "token",
                                currentToken
                            )
                            .put(
                                "intents",
                                0
                            )
                            .put(
                                "properties",
                                JSONObject()
                                    .put(
                                        "os",
                                        "windows"
                                    )
                                    .put(
                                        "browser",
                                        "Discord Client"
                                    )
                                    .put(
                                        "device",
                                        "desktop"
                                    )
                            )
                    )
                    .toString()
                    .let(socket::send)
            }.getOrElse {
                listener?.onError(
                    it.message
                        ?: "Failed to identify with Discord",
                    it
                )

                false
            }
        }

        private fun resume(
            socket: WebSocket
        ): Boolean {
            val currentToken =
                token?.takeIf {
                    it.isNotBlank()
                } ?: return false

            val currentSequence =
                sequence ?: return false

            if (sessionId.isBlank()) {
                return false
            }

            return runCatching {
                JSONObject()
                    .put("op", 6)
                    .put(
                        "d",
                        JSONObject()
                            .put(
                                "token",
                                currentToken
                            )
                            .put(
                                "session_id",
                                sessionId
                            )
                            .put(
                                "seq",
                                currentSequence
                            )
                    )
                    .toString()
                    .let(socket::send)
            }.getOrDefault(false)
        }

        private fun invalidSession() {
            if (
                isStopping ||
                !isRunning
            ) {
                return
            }

            stopHeartbeat()

            sequence = null
            sessionId = ""
            resumeGatewayUrl = null
            isReady = false

            invalidSessionJob?.cancel()

            invalidSessionJob =
                scope.launch {
                    delay(
                        INVALID_SESSION_DELAY
                            .milliseconds
                    )

                    if (
                        !isStopping &&
                        isRunning
                    ) {
                        webSocket?.let(
                            ::identify
                        )
                    }
                }
        }

        private fun dispatch(
            json: JSONObject
        ) {
            when (
                json.optString("t")
            ) {
                "READY" -> ready(json)
                "RESUMED" -> resumed()
            }
        }

        private fun ready(
            json: JSONObject
        ) {
            if (
                isStopping ||
                !isRunning
            ) {
                return
            }

            val data =
                json.optJSONObject("d")
                    ?: return

            data.optJSONObject("user")?.let {
                userInfo =
                    JSONObject(it.toString())
            }

            sessionId =
                data.optString(
                    "session_id",
                    ""
                )

            data.optString(
                "resume_gateway_url"
            )
                .takeIf {
                    it.isNotBlank()
                }
                ?.let {
                    resumeGatewayUrl =
                        if (it.contains("?")) {
                            it
                        } else {
                            "$it/?v=10&encoding=json"
                        }
                }

            isReady = true
            reconnectRequested = false
            reconnectDelay =
                INITIAL_RECONNECT_DELAY

            if (
                connectionMode ==
                ConnectionMode.USER_INFO
            ) {
                userInfoCallback?.invoke(
                    getUserInfo()
                )

                userInfoCallback = null

                stopConnection()
                return
            }

            listener?.onReady(
                getUserInfo()
            )

            sendPendingPresence()
        }

        private fun resumed() {
            if (
                isStopping ||
                !isRunning
            ) {
                return
            }

            isReady = true
            reconnectRequested = false
            reconnectDelay =
                INITIAL_RECONNECT_DELAY

            listener?.onReady(
                getUserInfo()
            )

            if (
                connectionMode ==
                ConnectionMode.USER_INFO
            ) {
                userInfoCallback?.invoke(
                    getUserInfo()
                )

                userInfoCallback = null

                stopConnection()
                return
            }

            sendPendingPresence()
        }

        override fun onFailure(
            webSocket: WebSocket,
            t: Throwable,
            response: Response?
        ) {
            if (
                !isCurrentSocket(webSocket)
            ) {
                return
            }

            synchronized(connectionLock) {
                isConnecting = false
                isReady = false

                if (
                    this@DiscordWsImplementation
                        .webSocket === webSocket
                ) {
                    this@DiscordWsImplementation
                        .webSocket = null
                }
            }

            stopHeartbeat()

            if (
                isStopping ||
                !isRunning
            ) {
                return
            }

            listener?.onConnectionFailed()
            scheduleReconnect()
        }

        override fun onClosing(
            webSocket: WebSocket,
            code: Int,
            reason: String
        ) {
            if (
                !isCurrentSocket(webSocket)
            ) {
                return
            }

            isReady = false
            stopHeartbeat()
        }

        override fun onClosed(
            webSocket: WebSocket,
            code: Int,
            reason: String
        ) {
            if (
                !isCurrentSocket(webSocket)
            ) {
                return
            }

            synchronized(connectionLock) {
                isConnecting = false
                isReady = false

                if (
                    this@DiscordWsImplementation
                        .webSocket === webSocket
                ) {
                    this@DiscordWsImplementation
                        .webSocket = null
                }
            }

            listener?.onClose(
                reason,
                code
            )

            stopHeartbeat()

            if (
                isStopping ||
                !isRunning
            ) {
                return
            }

            if (code == 4004) {
                stop()
                return
            }

            if (
                code == 4000 ||
                reconnectRequested
            ) {
                reconnectRequested = false
            }

            scheduleReconnect()
        }
    }

    companion object {
        private const val DEFAULT_GATEWAY_URL =
            "wss://gateway.discord.gg/?v=10&encoding=json"

        private const val INITIAL_RECONNECT_DELAY =
            1_000L

        private const val MAX_RECONNECT_DELAY =
            30_000L

        private const val INVALID_SESSION_DELAY =
            150L
    }
}