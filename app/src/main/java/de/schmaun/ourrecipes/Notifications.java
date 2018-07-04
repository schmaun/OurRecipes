package de.schmaun.ourrecipes;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import de.schmaun.ourrecipes.backup.GoogleDriveActivity;

public final class Notifications {
    public static Notification createBackupInProgress(Context context) {
        NotificationCompat.Builder builder = createBackupNotificationBuilder(context)
                        .setContentText(context.getText(R.string.backup_google_drive_notification_progress_message))
                        .setTicker(context.getText(R.string.backup_google_drive_notification_progress_message))
                        .setProgress(0, 0, true);

        return builder.build();
    }

    public static Notification createBackupFinished(Context context) {
        NotificationCompat.Builder builder = createBackupNotificationBuilder(context)
                .setContentText(context.getText(R.string.backup_google_drive_notification_finished_message))
                .setTicker(context.getText(R.string.backup_google_drive_notification_finished_message))
                .setProgress(0, 0, false);

        return builder.build();
    }

    public static Notification createBackupFailed(Context context) {
        NotificationCompat.Builder builder = createBackupNotificationBuilder(context)
                .setContentText(context.getText(R.string.backup_google_drive_notification_error_message))
                .setTicker(context.getText(R.string.backup_google_drive_notification_error_message))
                .setProgress(0, 0, false);

        return builder.build();
    }

    private static NotificationCompat.Builder createBackupNotificationBuilder(Context context)
    {
        Intent contentIntent = new Intent(context, GoogleDriveActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, contentIntent, 0);

        return new NotificationCompat.Builder(context, NotificationChannels.BACKUP_CHANNEL_ID)
                .setContentTitle(context.getText(R.string.backup_google_drive_notification_title))
                .setSmallIcon(R.drawable.chef_3)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW);
    }
}
