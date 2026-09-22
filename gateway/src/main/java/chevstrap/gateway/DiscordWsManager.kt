package chevstrap.gateway

import org.json.JSONObject

class DiscordWsManager {

    private var implementation: DiscordWsImplementation? = null

    private var listener:
            DiscordWsImplementation.DiscordWsListener? = null

    private var token: String? = null
    private var status: String? = null

    fun setListener(
        listener: DiscordWsImplementation.DiscordWsListener?
    ) {
        this.listener = listener
        implementation?.setListener(listener)
    }

    private fun create(): DiscordWsImplementation {
        return implementation
            ?: DiscordWsImplementation().also {
                it.setListener(listener)

                token?.let { value ->
                    it.token = value
                }

                status?.let { value ->
                    it.status = value
                }

                implementation = it
            }
    }

    fun getUserInfo(
        callback: (JSONObject?) -> Unit
    ) {
        create().startUserInfo(callback)
    }

    fun setSession(
        tokenVal: String,
        statusVal: String
    ) {
        token = tokenVal
        status = statusVal

        implementation?.apply {
            token = tokenVal
            status = statusVal
        }
    }

    fun setRPC(
        activityRPC: String
    ) {
        create().setPresence(activityRPC)
    }

    fun clearRPC() {
        implementation?.clearPresence()
    }

    fun stopRPC() {
        implementation?.stop()
        implementation = null
    }
}