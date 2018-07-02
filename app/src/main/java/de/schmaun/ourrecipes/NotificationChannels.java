package de.schmaun.ourrecipes;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public final class NotificationChannels {
    public static final String BACKUP_CHANNEL_ID = "1337";

    public static void create(Application application) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = application.getString(R.string.backup_notification_channel_name);
            String description = application.getString(R.string.backup_notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(BACKUP_CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = application.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
