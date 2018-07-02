package de.schmaun.ourrecipes;

import de.schmaun.ourrecipes.Utils.VolleyManager;

public class Application extends android.app.Application {
    @Override
    public void onCreate()
    {
        super.onCreate();
        VolleyManager.getInstance(this);
        NotificationChannels.create(this);
    }
}
