package de.schmaun.ourrecipes;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;


public class Dialogs {
    public static MaterialDialog showRestoreProgress(Context context) {
        return new MaterialDialog.Builder(context)
                .title(R.string.backup_google_drive_restore_dialog_title)
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .cancelable(false)
                .show();
    }

    public static MaterialDialog showRestoreFailed(Context context) {
        return new MaterialDialog.Builder(context)
                .title(R.string.backup_google_drive_restore_dialog_title)
                .content(R.string.backup_google_drive_restore_error_dialog_content)
                .positiveText(R.string.backup_google_drive_restore_finished_dialog_ok)
                .cancelable(false)
                .show();
    }

    public static MaterialDialog showRestoreSuccess(Context context) {
        return new MaterialDialog.Builder(context)
                .title(R.string.backup_google_drive_restore_dialog_title)
                .content(R.string.backup_google_drive_restore_finished_dialog_content)
                .positiveText(R.string.backup_google_drive_restore_finished_dialog_ok)
                .cancelable(false)
                .onAny((dialog1, which1) -> Application.restart(context))
                .show();
    }

    public static MaterialDialog showRestore(Context context, String title, MaterialDialog.SingleButtonCallback onPositive)
    {
        return new MaterialDialog.Builder(context)
                .title(R.string.backup_google_drive_restore_dialog_title)
                .content(String.format(context.getString(R.string.backup_google_drive_restore_dialog_content), title))
                .positiveText(R.string.backup_google_drive_restore_dialog_positive)
                .negativeText(R.string.backup_google_drive_restore_dialog_negative)
                .onPositive(onPositive)
                .show();
    }
}
