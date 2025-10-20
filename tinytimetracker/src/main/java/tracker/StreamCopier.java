/*
 * Created on Dec 8, 2004
 *
 * Copyright (c) 2004 iArchives
 * 
 * Stream Copier - copies data from an InputStream to an OutputStream
 * in a separate thread.
 */
package tracker;

import java.io.InputStream;
import java.io.OutputStream;


public class StreamCopier extends Thread {

    private InputStream in;
    private OutputStream out;

    public StreamCopier(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
        start();
    }

    public void run() {
        byte[] buf = new byte [1024];
        int len;
        try {
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static Process copyProcessStreams(Process p) {
        new StreamCopier(p.getInputStream(), System.out);
        new StreamCopier(p.getErrorStream(), System.err);
        return p;
    }
}