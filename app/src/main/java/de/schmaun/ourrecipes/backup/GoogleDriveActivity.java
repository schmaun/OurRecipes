package de.schmaun.ourrecipes.backup;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
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

import com.google.android.gms.drive.DriveId;

import java.util.ArrayList;

import de.schmaun.ourrecipes.Configuration;
import de.schmaun.ourrecipes.R;

public class GoogleDriveActivity extends AppCompatActivity implements BackupListAdapter.RestoreHandler {

    private static final String TAG = "GoogleDriveActivity";
    public static final int NUMBER_OF_BACKUPS_TO_DISPLAY = 10;
    private Button startBackupButton;
    private ToggleButton toggleInformationButton;
    private TextView lastRunInformationText;
    private RecyclerView backupListView;

    final Messenger messenger = new Messenger(new IncomingHandler());
    private Messenger messageService;

    private ServiceConnection backupServiceConnection = new BackupServiceConnection();
    private boolean isRunningBackup = false;

    class BackupServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName className, IBinder service) {
            messageService = new Messenger(service);

            sendMessageToService(Message.obtain(null, BackupService.MESSAGE_REGISTER));
            sendMessageToService(Message.obtain(null, BackupService.MESSAGE_BACKUP_STATUS));
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG, "onServiceDisconnected");
        }
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case BackupService.MESSAGE_PLEASE_UNBIND_FROM_ME:
                    Log.d(TAG, "handleMessage: MESSAGE_PLEASE_UNBIND_FROM_ME");
                    unbindService(backupServiceConnection);
                    backupServiceConnection = null;
                    break;
                case BackupService.MESSAGE_BACKUP_STATUS:
                    isRunningBackup = message.getData().getBoolean("data");
                    updateView();
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

        toggleInformationButton.setOnClickListener(v -> {
            ToggleButton button = (ToggleButton) v;
            if (button.isChecked()) {
                lastRunInformationText.setVisibility(View.VISIBLE);
            } else {
                lastRunInformationText.setVisibility(View.GONE);
            }
        });

        backupListView = findViewById(R.id.backup_google_drive_backups_to_restore);
        backupListView.setAdapter(createBackupListAdapter(new ArrayList<>()));
        backupListView.setHasFixedSize(true);
        backupListView.setNestedScrollingEnabled(false);
        ViewCompat.setNestedScrollingEnabled(backupListView, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        backupListView.setLayoutManager(layoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(backupListView.getContext(), LinearLayoutManager.VERTICAL);
        backupListView.addItemDecoration(dividerItemDecoration);

        updateView();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (backupServiceConnection == null) {
            backupServiceConnection = new BackupServiceConnection();
        }

        bindService(new Intent(this, BackupService.class), backupServiceConnection, BIND_AUTO_CREATE);
        sendMessageToService(Message.obtain(null, BackupService.MESSAGE_BACKUP_STATUS));
    }

    @Override
    protected void onStop() {
        super.onStop();

        sendMessageToService(Message.obtain(null, BackupService.MESSAGE_UNREGISTER));

        if (backupServiceConnection != null) {
            unbindService(backupServiceConnection);
            backupServiceConnection = null;
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

    public void startDatabaseRestore(DriveId backupFolderId,  GoogleDriveBackup.OnResultListener restoreFinishedCallback) {
        GoogleDriveBackup googleDriveBackup = new GoogleDriveBackup(this);
        googleDriveBackup.restoreDatabase(backupFolderId, restoreFinishedCallback);
    }

    public void startImagesRestore() {
        Intent intent = new Intent(this, BackupService.class);
        intent.setAction(BackupService.ACTION_RESTORE_IMAGES);
        startService(intent);

        bindService(new Intent(this, BackupService.class), backupServiceConnection, BIND_AUTO_CREATE);
    }

    private void updateView() {
        SharedPreferences sharedPref = getSharedPreferences(Configuration.PREFERENCES_NAME, Context.MODE_PRIVATE);
        long lastBackupDate = sharedPref.getLong(Configuration.PREF_KEY_LAST_BACKUP_DATE, 0L);
        int backupStatus = sharedPref.getInt(Configuration.PREF_KEY_BACKUP_STATUS, Configuration.PREF_KEY_BACKUP_STATUS_SUCCESS);

        updateLastBackupDateView(lastBackupDate);
        updateViewCurrentStatus();
        updateViewLastRunStatus(sharedPref, backupStatus);
        updateViewRestore();
    }

    private void updateViewLastRunStatus(SharedPreferences sharedPref, int backupStatus) {
        TextView statusText = (TextView) findViewById(R.id.backup_google_drive_status_text);
        statusText.setVisibility(View.GONE);
        toggleInformationButton.setVisibility(View.GONE);
        lastRunInformationText.setVisibility(View.GONE);
        if (backupStatus == Configuration.PREF_KEY_BACKUP_STATUS_ERROR) {
            statusText.setText(R.string.backup_google_drive_status_error);
            statusText.setVisibility(View.VISIBLE);
            toggleInformationButton.setVisibility(View.VISIBLE);
            lastRunInformationText.setVisibility(View.VISIBLE);
            lastRunInformationText.setText(sharedPref.getString(Configuration.PREF_KEY_LAST_BACKUP_MESSAGE, ""));
        }
    }

    private void updateViewCurrentStatus() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.backup_google_drive_progressbar);
        startBackupButton.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        if (isRunningBackup) {
            startBackupButton.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private void updateLastBackupDateView(long lastBackupDate) {
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
    }

    private void updateViewRestore() {
        GoogleDriveBackup googleDriveBackup = new GoogleDriveBackup(this);
        googleDriveBackup.loadBackups(NUMBER_OF_BACKUPS_TO_DISPLAY, new GoogleDriveBackup.LoadBackupsOnResultListener() {
            @Override
            public void onSuccess(ArrayList<Backup> backups) {
                Log.d(TAG, "updateViewRestore: display loaded backups");
                backupListView.swapAdapter(createBackupListAdapter(backups), true);
                ViewCompat.setNestedScrollingEnabled(backupListView, false);
            }

            @Override
            public void onError(Exception e) {
            }
        });
    }

    @NonNull
    private BackupListAdapter createBackupListAdapter(ArrayList<Backup> backups) {
        return new BackupListAdapter(GoogleDriveActivity.this, GoogleDriveActivity.this, backups);
    }
}
