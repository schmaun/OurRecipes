package de.schmaun.ourrecipes.backup;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import de.schmaun.ourrecipes.NotificationChannels;
import de.schmaun.ourrecipes.R;


public class BackupService extends IntentService {
    private static final String ACTION_BACKUP = "de.schmaun.ourrecipes.backup.action.BACKUP";
    private static final String ACTION_RESTORE = "de.schmaun.ourrecipes.backup.action.RESTORE";

    static final int JOB_ID_BACKUP = 1001;
    static final int JOB_ID_RESTORE = 1002;
    private static final int BACKUP_GOOGLE_DRIVE_FINISHED_NOTIFICATION_ID = 1000;
    private static final int BACKUP_GOOGLE_DRIVE_PROGRESS_NOTIFICATION_ID = 1001;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public BackupService() {
        super("BackupService");
    }

    public static void startActionBackup(Context context) {
        Intent intent = new Intent(context, BackupService.class);
        intent.setAction(ACTION_BACKUP);

        //enqueueWork(context, BackupService.class, JOB_ID_BACKUP, intent);

        context.startService(intent);
    }

    public static void startActionRestore(Context context) {
        Intent intent = new Intent(context, BackupService.class);
        intent.setAction(ACTION_RESTORE);

        //enqueueWork(context, BackupService.class, JOB_ID_RESTORE, intent);

        context.startService(intent);

    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_BACKUP.equals(action)) {
                handleActionBackup();
            } else if (ACTION_RESTORE.equals(action)) {
                handleActionRestore();
            }
        }
    }

    protected void onHandleWork(@NonNull Intent intent) {
        final String action = intent.getAction();
        if (ACTION_BACKUP.equals(action)) {
            handleActionBackup();
        } else if (ACTION_RESTORE.equals(action)) {
            handleActionRestore();
        }
    }

    private void handleActionBackup() {
        Intent contentIntent = new Intent(this, GoogleDriveActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, contentIntent, 0);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, NotificationChannels.BACKUP_CHANNEL_ID)
                        .setContentTitle(getText(R.string.backup_google_drive_notification_title))
                        .setContentText(getText(R.string.backup_google_drive_notification_progress_message))
                        .setSmallIcon(R.drawable.chef_3)
                        .setContentIntent(pendingIntent)
                        .setTicker(getText(R.string.backup_google_drive_notification_progress_message))
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .setProgress(0, 0, true);

        startForeground(BACKUP_GOOGLE_DRIVE_PROGRESS_NOTIFICATION_ID, builder.build());


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        int progress = 0;
        do {
            try {
                Thread.sleep(100);
                progress++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            builder.setProgress(100, progress, false);
            notificationManager.notify(BACKUP_GOOGLE_DRIVE_PROGRESS_NOTIFICATION_ID, builder.build());
        } while (progress < 100);

        builder.setProgress(0, 0, false)
                .setContentTitle(getText(R.string.backup_google_drive_notification_title))
                .setContentText(getText(R.string.backup_google_drive_notification_finished_message))
                .setTicker(getText(R.string.backup_google_drive_notification_finished_message));

        notificationManager.notify(BACKUP_GOOGLE_DRIVE_FINISHED_NOTIFICATION_ID, builder.build());
    }



    private void handleActionRestore() {
    }

}
