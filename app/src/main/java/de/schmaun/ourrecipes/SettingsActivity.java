package de.schmaun.ourrecipes;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragment {
        private static final String BACKUP_TO_GOOGLE_DRIVE_ACCOUNT_NAME = "backup_to_google_drive_account_name";
        private static final String BACKUP_TO_GOOGLE_DRIVE = "backup_to_google_drive";
        private static final String VERSION = "version";

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public void onResume() {
            super.onResume();

            setSummaryVersion();
            setSummaryGoogleDriveAccountName();
        }

        private void setSummaryVersion() {
            Preference appVersion = findPreference(VERSION);
            appVersion.setSummary(BuildConfig.VERSION_NAME);
        }

        private void setSummaryGoogleDriveAccountName() {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getActivity());
            Preference backupToGoogleDriveAccountName = findPreference(BACKUP_TO_GOOGLE_DRIVE_ACCOUNT_NAME);
            Preference backupToGoogleDrive = findPreference(BACKUP_TO_GOOGLE_DRIVE);
            if (account != null) {
                backupToGoogleDriveAccountName.setSummary(String.format("%s (%s)", account.getDisplayName(), account.getEmail()));
                backupToGoogleDrive.setSummary(getString(R.string.pref_description_backup_to_google_drive_status));
                backupToGoogleDrive.setEnabled(true);
            } else {
                backupToGoogleDriveAccountName.setSummary(getString(R.string.pref_description_backup_to_google_drive_account_na));
                backupToGoogleDrive.setSummary(getString(R.string.pref_description_backup_to_google_drive_status_not_logged_in));
            }
        }
    }
}
