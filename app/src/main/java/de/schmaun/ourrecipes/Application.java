package de.schmaun.ourrecipes;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.drive.DriveId;

import de.schmaun.ourrecipes.Utils.VolleyManager;
import de.schmaun.ourrecipes.backup.GoogleDriveActivity;

public class Application extends android.app.Application {
    public static final String RESTORE_DRIVE_ID = "restoreDriveId";
    public static final String RESTORE_DRIVE_ID2 = "restoreDriveId2";

    @Override
    public void onCreate()
    {
        super.onCreate();
        VolleyManager.getInstance(this);
        NotificationChannels.create(this);
    }

    public static void restartWithImageRestore(Context context, DriveId driveId)
    {
        Intent intent = new Intent(context, GoogleDriveActivity.class);
        intent.putExtra(RESTORE_DRIVE_ID, driveId.encodeToString());
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, 1337, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }
}
