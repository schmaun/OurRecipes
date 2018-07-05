package de.schmaun.ourrecipes.backup;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;

import java.util.Date;

import de.schmaun.ourrecipes.Configuration;
import de.schmaun.ourrecipes.Notifications;


public class BackupService extends IntentService {
//public class BackupService extends IntentService {
    private static final String ACTION_BACKUP = "de.schmaun.ourrecipes.backup.action.BACKUP";
    private static final String ACTION_RESTORE = "de.schmaun.ourrecipes.backup.action.RESTORE";
    public static final String ACTION_BACKUP_NOTIFY = "de.schmaun.ourrecipes.backup.action.BACKUP_NOTIFY";

    static final int JOB_ID_BACKUP = 1001;
    static final int JOB_ID_RESTORE = 1002;
    private static final int BACKUP_GOOGLE_DRIVE_FINISHED_NOTIFICATION_ID = 1000;
    private static final int BACKUP_GOOGLE_DRIVE_PROGRESS_NOTIFICATION_ID = 1001;
    private static final String TAG = "BackupService";

    public BackupService() {
        super("BackupService");
    }

    public static void startActionBackup(Context context) {
        Intent intent = new Intent(context, BackupService.class);
        intent.setAction(ACTION_BACKUP);

        context.startService(intent);
    }

    public static void startActionRestore(Context context) {
        Intent intent = new Intent(context, BackupService.class);
        intent.setAction(ACTION_RESTORE);

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
        updatePreferencesRunning();
        LocalBroadcastManager.getInstance(this).sendBroadcast((new Intent()).setAction(ACTION_BACKUP_NOTIFY));

        startForeground(BACKUP_GOOGLE_DRIVE_PROGRESS_NOTIFICATION_ID, Notifications.createBackupInProgress(this));

        GoogleDriveBackup googleDriveBackup = new GoogleDriveBackup(this);
        googleDriveBackup.doBackup(new GoogleDriveBackup.OnResultListener() {
            @Override
            public void onSuccess() {
                BackupService.this.onSuccess();
            }

            @Override
            public void onError(Exception e) {
                BackupService.this.onError(e);
            }
        });

        LocalBroadcastManager.getInstance(this).sendBroadcast((new Intent()).setAction(ACTION_BACKUP_NOTIFY));
    }

    private void onSuccess() {
        updatePreferencesSuccess();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(BACKUP_GOOGLE_DRIVE_FINISHED_NOTIFICATION_ID, Notifications.createBackupFinished(this));
    }

    private void onError(Exception e) {
        updatePreferencesError(e);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(BACKUP_GOOGLE_DRIVE_FINISHED_NOTIFICATION_ID, Notifications.createBackupFailed(this));
    }

    private void updatePreferencesError(Exception e) {
        getPreferencesEditor()
                .putInt(Configuration.PREF_KEY_BACKUP_STATUS, Configuration.PREF_KEY_BACKUP_STATUS_ERROR)
                .putString(Configuration.PREF_KEY_LAST_BACKUP_MESSAGE, e.getMessage())
                .apply();
    }

    private void updatePreferencesSuccess() {
        getPreferencesEditor()
                .putInt(Configuration.PREF_KEY_BACKUP_STATUS, Configuration.PREF_KEY_BACKUP_STATUS_SUCCESS)
                .putString(Configuration.PREF_KEY_LAST_BACKUP_MESSAGE, "Success")
                .putLong(Configuration.PREF_KEY_LAST_BACKUP_DATE, (new Date()).getTime())
                .apply();
    }

    private void updatePreferencesRunning() {
        getPreferencesEditor()
                .putInt(Configuration.PREF_KEY_BACKUP_STATUS, Configuration.PREF_KEY_BACKUP_STATUS_RUNNING)
                .apply();
    }

    private SharedPreferences.Editor getPreferencesEditor() {
        SharedPreferences sharedPref = getSharedPreferences(Configuration.PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sharedPref.edit();
    }

    private void handleActionRestore() {
    }


}
