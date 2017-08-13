package com.aruld.jersey.streaming;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * Media streaming utility
 *
 * @author Arul Dhesiaseelan (arul@httpmine.org)
 */
public class MediaStreamer implements StreamingOutput {

    private int length;
    private RandomAccessFile raf;
    private final byte[] buf = new byte[4096];

    public MediaStreamer(int length, RandomAccessFile raf) {
        this.length = length;
        this.raf = raf;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException, WebApplicationException {
        try {
            while( length != 0) {
                int read = raf.read(buf, 0, buf.length > length ? length : buf.length);
                outputStream.write(buf, 0, read);
                length -= read;
            }
        } catch (IOException e) {
            // Ignore EOF write/flush when client aborts a connection
            if (!(e instanceof EOFException)) {
                throw e;
            }

        } finally {
            raf.close();
        }
    }

    int getLength() {
        return length;
    }
}