package com.chevstrap.rbx.UI;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import androidx.core.app.NotificationCompat;

import com.chevstrap.rbx.App;
import com.chevstrap.rbx.CustomWatcher;
import com.chevstrap.rbx.Logger;
import com.chevstrap.rbx.Models.Entities.ActivityData;
import com.chevstrap.rbx.Models.Entities.UniverseDetails;
import com.chevstrap.rbx.R;

public class NotifyIconWrapper {
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final int NOTIFICATION_ID = 1001;

    private static void runOnUiThread(Runnable action) {
        mainHandler.post(action);
    }

    public static void showConnectionNotification(Context context, String ip, String universeStr) {
        Logger logger = App.getLogger();
        logger.writeLine("NotifyIconWrapper::showConnectionNotification", "Attempting to show connection notification for IP: " + ip + ", Universe: " + universeStr);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) {
            logger.writeLine("NotifyIconWrapper::showConnectionNotification", "NotificationManager is null, aborting notification.");
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                "rbx_connection_channel",
                "Roblox Connection",
                NotificationManager.IMPORTANCE_HIGH
        );
        notificationManager.createNotificationChannel(channel);
        logger.writeLine("NotifyIconWrapper::showConnectionNotification", "Notification channel created.");

        ActivityData data = CustomWatcher.getInstance().getActivityData();
        if (data == null) {
            logger.writeLine("NotifyIconWrapper::showConnectionNotification", "ActivityData is null, cannot proceed.");
            return;
        }

        try {
            long universeId = Long.parseLong(universeStr);
            logger.writeLine("NotifyIconWrapper::showConnectionNotification", "Universe ID set to: " + universeId);
        } catch (NumberFormatException e) {
            logger.writeLine("NotifyIconWrapper::showConnectionNotification", "Invalid universe ID: " + universeStr);
        }

        logger.writeLine("NotifyIconWrapper::showConnectionNotification", "Querying server location...");
        data.queryServerLocation(new ActivityData.LocationCallback() {
            @Override
            public void onLocationResolved(String location) {
                logger.writeLine("NotifyIconWrapper::showConnectionNotification", data.getGameHistoryDescription());
                logger.writeLine("NotifyIconWrapper::showConnectionNotification", "Server location resolved: " + location);

                runOnUiThread(() -> {
                    Bitmap bigIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.chevstrap_logo);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "rbx_connection_channel")
                            .setSmallIcon(R.drawable.chevstrap_logo)
                            .setLargeIcon(bigIcon)
                            .setContentTitle(App.getTextLocale(App.getAppContext(), R.string.notification_connected_to_a_server))
                            .setContentText(location)
                            .setAutoCancel(true)
                            .setPriority(NotificationCompat.PRIORITY_HIGH);

                    notificationManager.notify(NOTIFICATION_ID, builder.build());
                    logger.writeLine("NotifyIconWrapper::showConnectionNotification", "Connection notification displayed.");
                });
            }

            @Override
            public void onFailure() {
                logger.writeLine("NotifyIconWrapper::showConnectionNotification", "Server location query failed.");

                runOnUiThread(() -> {
                    Bitmap bigIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.chevstrap_logo);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "rbx_connection_channel")
                            .setSmallIcon(R.drawable.chevstrap_logo)
                            .setLargeIcon(bigIcon)
                            .setContentTitle(App.getTextLocale(App.getAppContext(), R.string.notification_connected_to_a_server))
                            .setContentText(App.getTextLocale(App.getAppContext(), R.string.notification_server_location_failed))
                            .setAutoCancel(true)
                            .setPriority(NotificationCompat.PRIORITY_HIGH);

                    notificationManager.notify(NOTIFICATION_ID, builder.build());
                    logger.writeLine("NotifyIconWrapper::showConnectionNotification", "Failure notification displayed.");
                });
            }
        });
    }

    public static void hideConnectionNotification(Context context) {
        Logger logger = App.getLogger();
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID);
            logger.writeLine("NotifyIconWrapper::hideConnectionNotification", "Connection notification hidden.");
        } else {
            logger.writeLine("NotifyIconWrapper::hideConnectionNotification", "NotificationManager is null, cannot hide notification.");
        }
    }
}
