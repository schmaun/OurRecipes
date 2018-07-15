package de.schmaun.ourrecipes.backup;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.schmaun.ourrecipes.Adapter.SimpleRecipeImageAdapter;
import de.schmaun.ourrecipes.Configuration;
import de.schmaun.ourrecipes.R;

public class GoogleDriveActivity extends AppCompatActivity {

    private static final String TAG = "GoogleDriveActivity";
    private Button startBackupButton;
    //private Button cancelBackupButton;
    private android.content.BroadcastReceiver broadcastReceiver;
    private ToggleButton toggleInformationButton;
    private TextView lastRunInformationText;
    private Button startRestoreButton;
    private TextView restoreLatesBackupInformation;
    private RecyclerView backupListView;

    final Messenger messenger = new Messenger(new IncomingHandler());
    private Messenger messageService;
    BackupService backupService;

    private ServiceConnection backupServiceConnection = new BackupServiceConnection();
    private boolean isRunning = false;

    class BackupServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName className, IBinder service) {
            messageService = new Messenger(service);

            sendMessageToService(Message.obtain(null, BackupService.MESSAGE_REGISTER));
            sendMessageToService(Message.obtain(null, BackupService.MESSAGE_STATUS));
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG, "onServiceDisconnected");
        }
    }

    ;

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case BackupService.MESSAGE_PLEASE_UNBIND_FROM_ME:
                    Log.d(TAG, "handleMessage: MESSAGE_PLEASE_UNBIND_FROM_ME");
                    unbindService(backupServiceConnection);
                    break;
                case BackupService.MESSAGE_STATUS:
                    isRunning = message.getData().getBoolean("data");

                    int backupStatus = Configuration.PREF_KEY_BACKUP_STATUS_SUCCESS;
                    if (isRunning) {
                        backupStatus = Configuration.PREF_KEY_BACKUP_STATUS_RUNNING;
                    }

                    updateView(backupStatus);
                    break;
                default:
                    super.handleMessage(message);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_google_drive);

        startBackupButton = (Button) findViewById(R.id.backup_google_drive_start_backup);
        toggleInformationButton = (ToggleButton) findViewById(R.id.backup_google_drive_show_last_run_information);
        startBackupButton.setOnClickListener(v -> startBackup());
        lastRunInformationText = (TextView) findViewById(R.id.backup_google_drive_last_run_information);

        restoreLatesBackupInformation = (TextView) findViewById(R.id.backup_google_drive_restore_last_backup_info);

        toggleInformationButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ToggleButton button = (ToggleButton) v;
                if (button.isChecked()) {
                    lastRunInformationText.setVisibility(View.VISIBLE);
                } else {
                    lastRunInformationText.setVisibility(View.GONE);
                }
            }
        });

        backupListView = findViewById(R.id.backup_google_drive_backups_to_restore);
        backupListView.setHasFixedSize(true);
        backupListView.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(backupListView.getContext(), LinearLayoutManager.VERTICAL);
        backupListView.addItemDecoration(dividerItemDecoration);

        updateView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onStart() {
        super.onStart();

        bindService(new Intent(this, BackupService.class), backupServiceConnection, BIND_AUTO_CREATE);

        sendMessageToService(Message.obtain(null, BackupService.MESSAGE_STATUS));
    }

    @Override
    protected void onStop() {
        super.onStop();

        sendMessageToService(Message.obtain(null, BackupService.MESSAGE_UNREGISTER));

        if (backupServiceConnection != null) {
            unbindService(backupServiceConnection);
        }
    }

    private void sendMessageToService(Message msg) {
        if (messageService != null) {
            try {
                msg.replyTo = messenger;
                messageService.send(msg);
            } catch (RemoteException ignored) {
            }
        }
    }

    private void startBackup() {
        Intent intent = new Intent(this, BackupService.class);
        intent.setAction(BackupService.ACTION_BACKUP);
        startService(intent);

        bindService(new Intent(this, BackupService.class), backupServiceConnection, BIND_AUTO_CREATE);
    }

    private void updateView() {
        //SharedPreferences sharedPref = getSharedPreferences(Configuration.PREFERENCES_NAME, Context.MODE_PRIVATE);

        //int backupStatus = sharedPref.getInt(Configuration.PREF_KEY_BACKUP_STATUS, 0);

/*
        int backupStatus = Configuration.PREF_KEY_BACKUP_STATUS_SUCCESS;
        if (backupService != null) {
            if (backupService.isRunning()) {
                backupStatus = Configuration.PREF_KEY_BACKUP_STATUS_RUNNING;
            }
        }
*/

        updateView(Configuration.PREF_KEY_BACKUP_STATUS_SUCCESS);
    }

    private void updateView(int backupStatus) {
        SharedPreferences sharedPref = getSharedPreferences(Configuration.PREFERENCES_NAME, Context.MODE_PRIVATE);
        long lastBackupDate = sharedPref.getLong(Configuration.PREF_KEY_LAST_BACKUP_DATE, 0L);

        String lastBackupDateString = getString(R.string.backup_google_drive_last_backup_completed_at_na);
        if (lastBackupDate > 0) {
            lastBackupDateString = DateUtils.formatDateTime(
                    getApplicationContext(),
                    lastBackupDate,
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
            );
        }

        TextView lastBackup = (TextView) findViewById(R.id.backup_google_drive_last_backup);
        lastBackup.setText(String.format(getString(R.string.backup_google_drive_last_backup_completed_at), lastBackupDateString));

        TextView statusText = (TextView) findViewById(R.id.backup_google_drive_status_text);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.backup_google_drive_progressbar);

        switch (backupStatus) {
            case Configuration.PREF_KEY_BACKUP_STATUS_SUCCESS:
                startBackupButton.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                statusText.setText("");
                statusText.setVisibility(View.GONE);
                toggleInformationButton.setVisibility(View.GONE);
                lastRunInformationText.setVisibility(View.GONE);
                lastRunInformationText.setText(sharedPref.getString(Configuration.PREF_KEY_LAST_BACKUP_MESSAGE, ""));
                break;
            case Configuration.PREF_KEY_BACKUP_STATUS_ERROR:
                startBackupButton.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                statusText.setText(R.string.backup_google_drive_status_error);
                statusText.setVisibility(View.VISIBLE);
                toggleInformationButton.setVisibility(View.VISIBLE);
                lastRunInformationText.setVisibility(View.GONE);
                lastRunInformationText.setText(sharedPref.getString(Configuration.PREF_KEY_LAST_BACKUP_MESSAGE, ""));
                break;
            case Configuration.PREF_KEY_BACKUP_STATUS_RUNNING:
                startBackupButton.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                statusText.setText(R.string.backup_google_drive_status_running);
                statusText.setVisibility(View.VISIBLE);
                toggleInformationButton.setVisibility(View.GONE);
                lastRunInformationText.setVisibility(View.GONE);
                break;
        }

        updateRestoreView();
    }

    private void updateRestoreView() {
        boolean restorePossible = true;
        Long backupToBeRestoredDate = new Date().getTime();


        GoogleDriveBackup googleDriveBackup = new GoogleDriveBackup(this);
        googleDriveBackup.loadBackups(new GoogleDriveBackup.LoadBackupsOnResultListener() {
            @Override
            public void onSuccess(ArrayList<Backup> backups) {
                backupListView.setAdapter(new BackupListAdapter(getApplicationContext(), backups));
            }

            @Override
            public void onError(Exception e) {
            }
        });


        /*
        if (restorePossible) {
            startRestoreButton.setEnabled(true);

            String readableBackupToBeRestoredDate = getString(R.string.backup_google_drive_restore_last_backup_at_na);
            readableBackupToBeRestoredDate = DateUtils.formatDateTime(
                    getApplicationContext(),
                    backupToBeRestoredDate,
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
            );
            restoreLatesBackupInformation.setText(String.format(getString(R.string.backup_google_drive_restore_last_backup_info), readableBackupToBeRestoredDate));
        }
        */
    }

    private class BroadcastReceiver extends android.content.BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            updateView();
        }
    }
}
