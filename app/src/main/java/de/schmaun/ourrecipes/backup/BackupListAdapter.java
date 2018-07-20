package de.schmaun.ourrecipes.backup;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.drive.DriveId;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import de.schmaun.ourrecipes.MainActivity;
import de.schmaun.ourrecipes.R;

public class BackupListAdapter extends RecyclerView.Adapter<BackupListAdapter.Holder> {

    private RestoreHandler restoreHandler;
    private final ArrayList<Backup> backups;
    private Context context;

    interface RestoreHandler {
        void startDatabaseRestore(DriveId backupFolderId, GoogleDriveBackup.OnResultListener onResultListener);
    }

    static class Holder extends RecyclerView.ViewHolder {
        private final TextView backupName;
        private final Button restoreButton;

        Holder(View itemView) {
            super(itemView);
            backupName = (TextView) itemView.findViewById(R.id.title);
            restoreButton = (Button) itemView.findViewById(R.id.restore);
        }
    }

    public BackupListAdapter(Context context, RestoreHandler restoreHandler, ArrayList<Backup> backups) {
        this.context = context;
        this.restoreHandler = restoreHandler;
        this.backups = backups;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.backup_list_item, parent, false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Backup backup = backups.get(position);

        String title = SimpleDateFormat.getDateTimeInstance().format(backup.getCreatedAt());
        holder.backupName.setText(title);
        holder.restoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(context)
                        .title(R.string.backup_google_drive_restore_dialog_title)
                        .content(String.format(context.getString(R.string.backup_google_drive_restore_dialog_content), title))
                        .positiveText(R.string.backup_google_drive_restore_dialog_positive)
                        .negativeText(R.string.backup_google_drive_restore_dialog_negative)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();

                                MaterialDialog progressDialog = new MaterialDialog.Builder(context)
                                        .title(R.string.backup_google_drive_restore_dialog_title)
                                        .progress(true, 0)
                                        .progressIndeterminateStyle(true)
                                        .cancelable(false)
                                        .show();

                                restoreHandler.startDatabaseRestore(backup.getFolderId(), new GoogleDriveBackup.OnResultListener() {
                                    @Override
                                    public void onSuccess() {
                                        progressDialog.dismiss();

                                        new MaterialDialog.Builder(context)
                                                .title(R.string.backup_google_drive_restore_dialog_title)
                                                .content(R.string.backup_google_drive_restore_finished_dialog_content)
                                                .positiveText(R.string.backup_google_drive_restore_finished_dialog_ok)
                                                .cancelable(false)
                                                .onAny(new MaterialDialog.SingleButtonCallback() {
                                                    @Override
                                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                        PendingIntent mPendingIntent = PendingIntent.getActivity(context, 1337, new Intent(context, MainActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);

                                                        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                                                        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                                                        System.exit(0);
                                                    }
                                                })
                                                .show();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        progressDialog.dismiss();

                                        new MaterialDialog.Builder(context)
                                                .title(R.string.backup_google_drive_restore_dialog_title)
                                                .content(R.string.backup_google_drive_restore_error_dialog_content)
                                                .positiveText(R.string.backup_google_drive_restore_finished_dialog_ok)
                                                .cancelable(false)
                                                .show();
                                    }
                                });
                            }
                        })
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return backups == null ? 0 : backups.size();
    }

}
