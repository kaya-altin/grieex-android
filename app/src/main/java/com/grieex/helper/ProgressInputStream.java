package com.grieex.helper;

import java.io.IOException;
import java.io.InputStream;

public class ProgressInputStream extends InputStream {
    private final InputStream wrappedInputStream;
    private final long totalSize;
    private long counter;
    private final ProgressInputStream.Listener listener;

    public ProgressInputStream(InputStream in, long totalSize, ProgressInputStream.Listener listener) {
        wrappedInputStream = in;
        this.totalSize = totalSize;
        this.listener = listener;
    }


    @Override
    public int read() throws IOException {
        int retVal = wrappedInputStream.read();
        counter += 1;
        check(retVal);
        return retVal;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int retVal = wrappedInputStream.read(b);
        counter += retVal;
        check(retVal);
        return retVal;
    }

    @Override
    public int read(byte[] b, int offset, int length) throws IOException {
        int retVal = wrappedInputStream.read(b, offset, length);
        counter += retVal;
        check(retVal);
        return retVal;
    }

    private void check(int retVal) {
        if (retVal != -1) {
            int percent = (int) (counter * 100 / totalSize);
            listener.progress(percent);
        }
    }

    @Override
    public void close() throws IOException {
        wrappedInputStream.close();
    }

    @Override
    public int available() throws IOException {
        return wrappedInputStream.available();
    }

    @Override
    public void mark(int readlimit) {
        wrappedInputStream.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        wrappedInputStream.reset();
    }

    @Override
    public boolean markSupported() {
        return wrappedInputStream.markSupported();
    }

    @Override
    public long skip(long n) throws IOException {
        return wrappedInputStream.skip(n);
    }

    /**
     * Interface for classes that want to monitor this input stream
     */
    public interface Listener {
        void progress(int percent);
    }
}