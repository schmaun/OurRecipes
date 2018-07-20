package de.schmaun.ourrecipes.backup;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;

import de.schmaun.ourrecipes.Configuration;
import de.schmaun.ourrecipes.Notifications;


public class BackupService extends IntentService {
    public static final String ACTION_BACKUP = "de.schmaun.ourrecipes.backup.action.BACKUP";
    public static final String ACTION_RESTORE_IMAGES = "de.schmaun.ourrecipes.backup.action.RESTORE_IMAGES";

    public static final String ACTION_BACKUP_NOTIFY = "de.schmaun.ourrecipes.backup.action.BACKUP_NOTIFY";
    public static final String ACTION_BACKUP_RESTORE = "de.schmaun.ourrecipes.backup.action.RESTORE_NOTIFY";

    public static final int MESSAGE_REGISTER = 1;
    public static final int MESSAGE_UNREGISTER = 2;
    public static final int MESSAGE_BACKUP_STATUS = 3;
    public static final int MESSAGE_PLEASE_UNBIND_FROM_ME = 4;

    static final int JOB_ID_BACKUP = 1001;
    static final int JOB_ID_RESTORE = 1002;
    private static final int BACKUP_GOOGLE_DRIVE_FINISHED_NOTIFICATION_ID = 1000;
    private static final int BACKUP_GOOGLE_DRIVE_PROGRESS_NOTIFICATION_ID = 1001;
    private static final String TAG = "BackupService";

    private boolean running;
    ArrayList<Messenger> clients = new ArrayList<Messenger>();
    final Messenger messenger = new Messenger(new IncomingHandler());

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            Log.d(TAG, "handleMessage: " + Integer.toString(message.what));

            switch (message.what) {
                case MESSAGE_REGISTER:
                    clients.add(message.replyTo);
                    break;
                case MESSAGE_UNREGISTER:
                    clients.remove(message.replyTo);
                    break;
                case MESSAGE_BACKUP_STATUS:
                    sendCurrentStatusToClients();
                default:
                    super.handleMessage(message);
            }
        }
    }


    public BackupService() {
        super("BackupService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            String currentAction = intent.getAction();
            running = true;
            sendCurrentStatusToClients();
            if (ACTION_BACKUP.equals(currentAction)) {
                handleActionBackup();
            } else if (ACTION_RESTORE_IMAGES.equals(currentAction)) {
                handleActionRestoreImages();
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    private void handleActionBackup() {
        startForeground(BACKUP_GOOGLE_DRIVE_PROGRESS_NOTIFICATION_ID, Notifications.createBackupInProgress(this));

        GoogleDriveBackup googleDriveBackup = new GoogleDriveBackup(this);
        googleDriveBackup.backup(new GoogleDriveBackup.OnResultListener() {
            @Override
            public void onSuccess() {
                onSuccessBackup();
            }

            @Override
            public void onError(Exception e) {
                onErrorBackup(e);
            }
        });
    }

    private void handleActionRestoreImages() {
/*        startForeground(BACKUP_GOOGLE_DRIVE_PROGRESS_NOTIFICATION_ID, Notifications.restoreBackupInProgress(this));

        GoogleDriveBackup googleDriveBackup = new GoogleDriveBackup(this);
        googleDriveBackup.restoreImages(new GoogleDriveBackup.OnResultListener() {
            @Override
            public void onSuccess() {
                BackupService.this.onSuccessRestore();
            }

            @Override
            public void onError(Exception e) {
                BackupService.this.onErrorRestore(e);
            }
        });*/
    }

    private void onSuccessBackup() {
        updatePreferencesSuccess();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(BACKUP_GOOGLE_DRIVE_FINISHED_NOTIFICATION_ID, Notifications.createBackupFinished(this));

        finished();
    }

    private void onErrorBackup(Exception e) {
        updatePreferencesError(e);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(BACKUP_GOOGLE_DRIVE_FINISHED_NOTIFICATION_ID, Notifications.createBackupFailed(this));

        finished();
    }

    private void onSuccessRestore() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(BACKUP_GOOGLE_DRIVE_FINISHED_NOTIFICATION_ID, Notifications.restoreBackupFinished(this));
        notificationManager.cancel(BACKUP_GOOGLE_DRIVE_PROGRESS_NOTIFICATION_ID);

        finished();
    }

    private void onErrorRestore(Exception e) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(BACKUP_GOOGLE_DRIVE_FINISHED_NOTIFICATION_ID, Notifications.restoreBackupFailed(this));
        notificationManager.cancel(BACKUP_GOOGLE_DRIVE_PROGRESS_NOTIFICATION_ID);

        finished();
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

    private SharedPreferences.Editor getPreferencesEditor() {
        SharedPreferences sharedPref = getSharedPreferences(Configuration.PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sharedPref.edit();
    }

    private void finished() {
        running = false;

        sendCurrentStatusToClients();
        sendMessageToClients(Message.obtain(null, MESSAGE_PLEASE_UNBIND_FROM_ME));
        stopSelf();
    }

    private void sendCurrentStatusToClients() {
        Message replyMessage = Message.obtain(null, MESSAGE_BACKUP_STATUS);

        Bundle bundle = new Bundle();
        bundle.putBoolean("data", running);
        replyMessage.setData(bundle);

        sendMessageToClients(replyMessage);
    }

    private void sendMessageToClients(Message message) {
        Log.d(TAG, "sendMessageToClients: " + Integer.toString(message.what));

        for (int i = clients.size() - 1; i >= 0; i--) {
            try {
                clients.get(i).send(message);
                Log.d(TAG, "sendMessageToClients: client " + Integer.toString(i));
            } catch (RemoteException e) {
                clients.remove(i);
                Log.d(TAG, "sendMessageToClients: removed client");
            }
        }
    }
}
