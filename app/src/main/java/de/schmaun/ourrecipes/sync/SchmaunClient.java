package de.schmaun.ourrecipes.sync;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.schmaun.ourrecipes.Utils.VolleyManager;

import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

public class SchmaunClient {
    private static final String BASE_URL = "http://ourrecipes.schmaun.de";
    private static final String TAG = "SchmaunClient";


    public interface SignInListener {
        void onSuccess(String token);

        void onError(Exception e);
    }

    public void signIn(String email, String password, final SignInListener signInListener) {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("email", email);
        parameters.put("password", password);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, BASE_URL + "/login", new JSONObject(parameters), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "signIn: onResponse");
                        try {
                            signInListener.onSuccess(response.getString("token"));
                        } catch (JSONException e) {
                            Log.d(TAG, "signIn: onResponse: error: " + e.getMessage());
                            signInListener.onError(e);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "signIn: onErrorResponse: error: " + error.getMessage());
                        signInListener.onError(error);
                    }
                });

        VolleyManager.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    public void signInBlocking(String email, String password, final SignInListener signInListener) {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("email", email);
        parameters.put("password", password);

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, BASE_URL + "/login", new JSONObject(parameters), future, future);

        VolleyManager.getInstance().addToRequestQueue(jsonObjectRequest);

        try {
            JSONObject response = future.get(30, TimeUnit.SECONDS);
            signInListener.onSuccess(response.getString("token"));
        } catch (InterruptedException | ExecutionException | JSONException | TimeoutException e) {
            Log.d(TAG, "signIn: signInBlocking: error: " + e.getMessage());

            signInListener.onError(e);
        }
    }
}
