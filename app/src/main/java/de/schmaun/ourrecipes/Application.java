package de.schmaun.ourrecipes;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import de.schmaun.ourrecipes.Utils.VolleyManager;

public class Application extends android.app.Application {
    @Override
    public void onCreate()
    {
        super.onCreate();
        VolleyManager.getInstance(this);
        NotificationChannels.create(this);
    }

    public static void restart(Context context)
    {
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, 1337, new Intent(context, MainActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }
}
