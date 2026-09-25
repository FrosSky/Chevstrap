package com.chevstrap.rbx.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.chevstrap.rbx.NotificationReceiver
import com.chevstrap.rbx.App
import com.chevstrap.rbx.R
import com.chevstrap.rbx.extensions.ResourceManagerEx
import com.chevstrap.rbx.models.entities.ActivityData

object NotifyIconWrapper {

    private val mainHandler = Handler(Looper.getMainLooper())

    private const val NOTIFICATION_ID = 1001
    private const val DISCORD_RPC_VISIBILITY_NOTIFICATION_ID = 1002

    private const val CHANNEL_ID = "chevstrap_connected_to_a_server"
    private const val CHANNEL_NAME = "Just a notification"

    fun runOnUiThread(action: Runnable) {
        mainHandler.post(action)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
    }

    @JvmStatic
    fun showConnectionNotification(
        context: Context,
        data: ActivityData,
        ip: String?,
        universeStr: String
    ) {
        val logger = App.logger

        logger.writeLine(
            "NotifyIconWrapper::showConnectionNotification",
            "Attempting to show connection notification for IP: $ip, Universe: $universeStr"
        )

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

        if (notificationManager == null) {
            logger.writeLine(
                "NotifyIconWrapper::showConnectionNotification",
                "NotificationManager is null, aborting notification."
            )
            return
        }

        // Buat channel menggunakan context yang dikirimkan
        createNotificationChannel(notificationManager)

        try {
            val universeId = universeStr.toLong()
            logger.writeLine(
                "NotifyIconWrapper::showConnectionNotification",
                "Universe ID set to: $universeId"
            )
        } catch (_: NumberFormatException) {
            logger.writeLine(
                "NotifyIconWrapper::showConnectionNotification",
                "Invalid universe ID: $universeStr"
            )
        }

        logger.writeLine(
            "NotifyIconWrapper::showConnectionNotification",
            "Querying server location..."
        )

        data.queryServerLocation(object : ActivityData.LocationCallback {

            override fun onLocationResolved(location: String?) {
                logger.writeLine(
                    "NotifyIconWrapper::showConnectionNotification",
                    data.gameHistoryDescription
                )

                logger.writeLine(
                    "NotifyIconWrapper::showConnectionNotification",
                    "Server location resolved: $location"
                )

                runOnUiThread {
                    val bigIcon = BitmapFactory.decodeResource(
                        context.resources,
                        R.drawable.chevstrap_icon
                    )

                    val titleText = ResourceManagerEx.getStringOrEmpty(
                        context,
                        R.string.notification_connected_to_a_server
                    ).ifEmpty { "Connected to server" }

                    val bodyText = if (!location.isNullOrBlank()) {
                        location
                    } else {
                        ResourceManagerEx.getStringOrEmpty(
                            context,
                            R.string.notification_server_location_failed
                        ).ifEmpty { "N/A" }
                    }

                    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.chevstrap_icon)
                        .setLargeIcon(bigIcon)
                        .setContentTitle(titleText)
                        .setContentText(bodyText)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)

                    notificationManager.notify(NOTIFICATION_ID, builder.build())

                    logger.writeLine(
                        "NotifyIconWrapper::showConnectionNotification",
                        "Connection notification displayed."
                    )
                }
            }

            override fun onFailure() {
                logger.writeLine(
                    "NotifyIconWrapper::showConnectionNotification",
                    "Server location query failed."
                )

                runOnUiThread {
                    val bigIcon = BitmapFactory.decodeResource(
                        context.resources,
                        R.drawable.chevstrap_icon
                    )

                    val titleText = ResourceManagerEx.getStringOrEmpty(
                        context,
                        R.string.notification_connected_to_a_server
                    ).ifEmpty { "Connected to server" }

                    val bodyText = ResourceManagerEx.getStringOrEmpty(
                        context,
                        R.string.notification_server_location_failed
                    ).ifEmpty { "Failed to retrieve server location" }

                    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.chevstrap_icon)
                        .setLargeIcon(bigIcon)
                        .setContentTitle(titleText)
                        .setContentText(bodyText)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)

                    notificationManager.notify(NOTIFICATION_ID, builder.build())

                    logger.writeLine(
                        "NotifyIconWrapper::showConnectionNotification",
                        "Failure notification displayed."
                    )
                }
            }
        })
    }

    @JvmStatic
    fun hideConnectionNotification(context: Context) {
        val logger = App.logger

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID)
            logger.writeLine(
                "NotifyIconWrapper::hideConnectionNotification",
                "Connection notification hidden."
            )
        } else {
            logger.writeLine(
                "NotifyIconWrapper::hideConnectionNotification",
                "NotificationManager is null, cannot hide notification."
            )
        }
    }

    @JvmStatic
    fun showDiscordRpcVisibilityNotification(
        context: Context,
        enabled: Boolean
    ) {
        val logger = App.logger

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

        if (notificationManager == null) {
            logger.writeLine(
                "NotifyIconWrapper::showDiscordRpcVisibilityNotification",
                "NotificationManager is null, aborting notification."
            )
            return
        }

        createNotificationChannel(notificationManager)

        val bigIcon = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.chevstrap_icon
        )

        val toggleIntent = Intent(
            NotificationReceiver.ACTION_TOGGLE_DISCORD_RPC_VISIBILITY
        ).apply {
            setPackage(context.packageName)
        }

        val togglePendingIntent = PendingIntent.getBroadcast(
            context,
            DISCORD_RPC_VISIBILITY_NOTIFICATION_ID,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val buttonTextRes = if (enabled) R.string.common_hide else R.string.common_show
        val buttonText = ResourceManagerEx.getStringOrEmpty(context, buttonTextRes)
            .ifEmpty { if (enabled) "Hide" else "Show" }

        val titleText = ResourceManagerEx.getStringOrEmpty(
            context,
            R.string.notification_discord_rich_presence_visibility_controller_title
        ).ifEmpty { "Discord Rich Presence" }

        val descText = ResourceManagerEx.getStringOrEmpty(
            context,
            R.string.notification_discord_rich_presence_visibility_controller_description
        ).ifEmpty { "Set the visibility of your Discord Rich Presence." }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.chevstrap_icon)
            .setLargeIcon(bigIcon)
            .setContentTitle(titleText)
            .setContentText(descText)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(
                R.drawable.chevstrap_icon,
                buttonText,
                togglePendingIntent
            )

        notificationManager.notify(
            DISCORD_RPC_VISIBILITY_NOTIFICATION_ID,
            builder.build()
        )

        logger.writeLine(
            "NotifyIconWrapper::showDiscordRpcVisibilityNotification",
            "Discord RPC visibility notification updated."
        )
    }

    @JvmStatic
    fun hideDiscordRpcVisibilityNotification(context: Context) {
        val logger = App.logger

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

        if (notificationManager != null) {
            notificationManager.cancel(DISCORD_RPC_VISIBILITY_NOTIFICATION_ID)
            logger.writeLine(
                "NotifyIconWrapper::hideDiscordRpcVisibilityNotification",
                "Discord RPC visibility notification hidden."
            )
        } else {
            logger.writeLine(
                "NotifyIconWrapper::hideDiscordRpcVisibilityNotification",
                "NotificationManager is null, cannot hide Discord RPC visibility notification."
            )
        }
    }
}