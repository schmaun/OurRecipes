package de.schmaun.ourrecipes.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class Stream {
    public static void copy(InputStream in, OutputStream outputStream) throws IOException {
        byte[] buf = new byte[8192];
        int c = 0;
        while ((c = in.read(buf, 0, buf.length)) > 0) {
            outputStream.write(buf, 0, c);
            outputStream.flush();
        }
        outputStream.close();
    }
}
