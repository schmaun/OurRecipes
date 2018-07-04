package de.schmaun.ourrecipes.Utils;

import android.webkit.MimeTypeMap;

final public class File {
    public static String getMimeTypeFromUrl(String uri)
    {
        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri);

        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
    }
}
