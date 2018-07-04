package de.schmaun.ourrecipes.backup;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;

import java.util.Date;

import de.schmaun.ourrecipes.Configuration;
import de.schmaun.ourrecipes.Notifications;


public class BackupService extends IntentService {
    private static final String ACTION_BACKUP = "de.schmaun.ourrecipes.backup.action.BACKUP";
    private static final String ACTION_RESTORE = "de.schmaun.ourrecipes.backup.action.RESTORE";

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
        SharedPreferences sharedPref = getSharedPreferences(Configuration.PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(Configuration.PREF_KEY_LAST_BACKUP_STATUS, false);
        editor.putString(Configuration.PREF_KEY_LAST_BACKUP_MESSAGE, e.getMessage());
        editor.apply();
    }

    private void updatePreferencesSuccess() {
        SharedPreferences sharedPref = getSharedPreferences(Configuration.PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(Configuration.PREF_KEY_LAST_BACKUP_STATUS, true);
        editor.putString(Configuration.PREF_KEY_LAST_BACKUP_MESSAGE, "Success");
        editor.putLong(Configuration.PREF_KEY_LAST_BACKUP_DATE, (new Date()).getTime());
        editor.apply();
    }

    private void handleActionRestore() {
    }

}
