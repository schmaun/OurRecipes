package de.schmaun.ourrecipes.backup;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.drive.DriveId;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import de.schmaun.ourrecipes.Application;
import de.schmaun.ourrecipes.Dialogs;
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
        holder.restoreButton.setOnClickListener(v -> {
            Dialogs.showRestore(context, title,
                    (dialog, which) -> {
                        dialog.dismiss();
                        MaterialDialog progressDialog = Dialogs.showRestoreProgress(context);
                        restoreHandler.startDatabaseRestore(backup.getFolderId(), new GoogleDriveBackup.OnResultListener() {
                            @Override
                            public void onSuccess() {
                                progressDialog.dismiss();
                                Dialogs.showRestoreSuccess(context, (dialog1, which1) -> {
                                    Application.restartWithImageRestore(context, backup.getFolderId());
                                });
                            }

                            @Override
                            public void onError(Exception e) {
                                progressDialog.dismiss();
                                Dialogs.showRestoreFailed(context);
                            }
                        });
                    });
        });
    }

    @Override
    public int getItemCount() {
        return backups == null ? 0 : backups.size();
    }

}
