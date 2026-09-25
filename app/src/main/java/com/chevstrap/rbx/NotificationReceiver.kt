package com.chevstrap.rbx

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.chevstrap.rbx.ui.NotifyIconWrapper

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_TOGGLE_DISCORD_RPC_VISIBILITY =
            "com.chevstrap.rbx.action.TOGGLE_DISCORD_RPC_VISIBILITY"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TOGGLE_DISCORD_RPC_VISIBILITY) return

        CustomWatcher.getInstance().robloxDiscordRPC?.let { robloxDiscordRPC ->
            val toThisVisible = !robloxDiscordRPC.isVisibleState()

            robloxDiscordRPC.setVisibility(toThisVisible)

            NotifyIconWrapper.showDiscordRpcVisibilityNotification(
                context,
                toThisVisible
            )
        }
    }
}