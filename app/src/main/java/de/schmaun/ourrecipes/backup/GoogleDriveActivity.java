package de.schmaun.ourrecipes.backup;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

import de.schmaun.ourrecipes.Configuration;
import de.schmaun.ourrecipes.R;

public class GoogleDriveActivity extends AppCompatActivity {

    private Button startBackup;

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

        updateView();
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
            Date date = new Date(lastBackupDate);
            DateFormat dateFormat = android.text.format.DateFormat.getLongDateFormat(getApplicationContext());
            lastBackupDateString = dateFormat.format(date);

            lastBackupDateString = DateUtils.formatDateTime(getApplicationContext(), lastBackupDate, DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR);
        }

        TextView statusView = (TextView) findViewById(R.id.backup_google_drive_status);
        statusView.setText(String.format(getString(R.string.backup_google_drive_last_backup_completed_at), lastBackupDateString));
    }
}
