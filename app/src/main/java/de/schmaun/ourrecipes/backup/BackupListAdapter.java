package de.schmaun.ourrecipes.backup;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import de.schmaun.ourrecipes.R;

public class BackupListAdapter extends RecyclerView.Adapter<BackupListAdapter.Holder> {

    private final ArrayList<Backup> backups;
    private Context context;

    static class Holder extends RecyclerView.ViewHolder {
        private final TextView backupName;
        private final Button restoreButton;

        Holder(View itemView) {
            super(itemView);
            backupName = (TextView) itemView.findViewById(R.id.title);
            restoreButton = (Button) itemView.findViewById(R.id.restore);
        }
    }

    public BackupListAdapter(Context context, ArrayList<Backup> backups) {
        this.context = context;
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
            String formattedDate = SimpleDateFormat.getDateTimeInstance().format(backup.getCreatedAt());
            /*String formattedDate = DateUtils.formatDateTime(
                    context,
                    backup.getCreatedAt(),
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR);
                    */

            new MaterialDialog.Builder(this.context)
                    .title(context.getString(R.string.backup_google_drive_restore_dialog_title))
                    .content(String.format(context.getString(R.string.backup_google_drive_restore_dialog_content), title))
                    .positiveText(context.getString(R.string.backup_google_drive_restore_dialog_positive))
                    .negativeText(context.getString(R.string.backup_google_drive_restore_dialog_negative))
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return backups == null ? 0 : backups.size();
    }

}
