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
import android.widget.TextView;

import de.schmaun.ourrecipes.Configuration;
import de.schmaun.ourrecipes.R;

public class GoogleDriveActivity extends AppCompatActivity {

    private Button startBackup;
    private android.content.BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_google_drive);

        startBackup = (Button) findViewById(R.id.backup_google_drive_start_backup);
        startBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBackup();
            }
        });

        broadcastReceiver = new BroadcastReceiver();
        IntentFilter filter = new IntentFilter(BackupService.ACTION_BACKUP_FINISHED);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter);

        updateView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    private void startBackup() {
        startBackup.setEnabled(false);
        BackupService.startActionBackup(this);
    }

    private void onFinishBackup() {
        startBackup.setEnabled(true);
        updateView();
    }

    private void updateView() {
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

        TextView statusView = (TextView) findViewById(R.id.backup_google_drive_status);
        statusView.setText(String.format(getString(R.string.backup_google_drive_last_backup_completed_at), lastBackupDateString));
    }

    private class BroadcastReceiver extends android.content.BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            onFinishBackup();
        }
    }
}
