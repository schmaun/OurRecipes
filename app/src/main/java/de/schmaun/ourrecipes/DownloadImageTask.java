package de.schmaun.ourrecipes;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

public class DownloadImageTask extends AsyncTask<Uri, Void, File> {
    public static final String TAG = "DownloadImageTask";
    private Exception error;
    private Context context;
    private DownloadImageHandler imageHandler;
    private File target;

    public interface DownloadImageHandler {
        void onError(Exception error);
        void onSuccess(File file);
    }

    public DownloadImageTask(Context context, DownloadImageHandler imageHandler, File target)
    {
        this.context = context;
        this.imageHandler = imageHandler;
        this.target = target;
    }

    protected File doInBackground(Uri... uris) {
        try {
            downloadImage(uris[0]);
        } catch (IOException e) {
            error = e;
            Log.e(TAG, e.getMessage(), e);
        }
        return target;
    }

    protected void onPostExecute(File file) {
        if (error != null) {
            imageHandler.onError(error);
        } else {
            imageHandler.onSuccess(file);
        }
    }

    private void downloadImage(Uri uri) throws IOException {
        InputStream in = context.getContentResolver().openInputStream(uri);
        OutputStream out = new FileOutputStream(this.target);

        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }

        in.close();
        out.close();
    }
}
