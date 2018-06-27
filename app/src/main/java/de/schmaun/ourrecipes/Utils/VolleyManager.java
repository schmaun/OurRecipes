package de.schmaun.ourrecipes.Utils;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleyManager {
    private static VolleyManager instance = null;

    private RequestQueue requestQueue;

    private VolleyManager(Context context) {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public static synchronized VolleyManager getInstance(Context context) {
        if (null == instance) {
            instance = new VolleyManager(context);
        }

        return instance;
    }

    //this is so you don't need to pass context each time
    public static synchronized VolleyManager getInstance() {
        if (null == instance) {
            throw new IllegalStateException(VolleyManager.class.getSimpleName() +
                    " is not initialized, call getInstance(...) first");
        }
        return instance;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        requestQueue.add(req);
    }
}