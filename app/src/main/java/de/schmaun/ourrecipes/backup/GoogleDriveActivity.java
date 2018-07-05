package de.schmaun.ourrecipes.backup;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import de.schmaun.ourrecipes.Configuration;
import de.schmaun.ourrecipes.R;

public class GoogleDriveActivity extends AppCompatActivity {

    private Button startBackupButton;
    private Button cancelBackupButton;
    private android.content.BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_google_drive);

        startBackupButton = (Button) findViewById(R.id.backup_google_drive_start_backup);
        startBackupButton.setOnClickListener(v -> startBackup());
        cancelBackupButton = (Button) findViewById(R.id.backup_google_drive_cancel_backup);
        cancelBackupButton.setOnClickListener(v -> cancelBackup());

        broadcastReceiver = new BroadcastReceiver();
        IntentFilter filter = new IntentFilter(BackupService.ACTION_BACKUP_NOTIFY);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter);

        updateView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    private void cancelBackup() {
    }

    private void startBackup() {
        BackupService.startActionBackup(this);
    }


    private void updateView() {
        SharedPreferences sharedPref = getSharedPreferences(Configuration.PREFERENCES_NAME, Context.MODE_PRIVATE);

        int backupStatus = sharedPref.getInt(Configuration.PREF_KEY_BACKUP_STATUS, 0);
        updateView(backupStatus);
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
                break;
            case Configuration.PREF_KEY_BACKUP_STATUS_ERROR:
                startBackupButton.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                statusText.setText(R.string.backup_google_drive_status_error);
                break;
            case Configuration.PREF_KEY_BACKUP_STATUS_RUNNING:
                startBackupButton.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                statusText.setText(R.string.backup_google_drive_status_running);
                break;
        }
    }

    private class BroadcastReceiver extends android.content.BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            updateView();
        }
    }
}
