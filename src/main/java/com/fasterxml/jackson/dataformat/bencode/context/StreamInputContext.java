package com.fasterxml.jackson.dataformat.bencode.context;

import java.io.IOException;
import java.io.InputStream;

public class StreamInputContext {
    private final InputStream in;

    public StreamInputContext(InputStream in) {
        this.in = in;
    }

    public long skip(long n) throws IOException {
        long skipped = 0, cSkipped;
        while (skipped < n && (cSkipped = in.skip(n - skipped)) > 0) skipped += cSkipped;
        return skipped;
    }

    public void close() throws IOException {
        in.close();
    }

    public void reset() throws IOException {
        in.reset();
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int sumLen = 0, currentLen;
        while ((sumLen < len) && (currentLen = in.read(b, off + sumLen, len - sumLen)) > 0) sumLen += currentLen;
        return sumLen;
    }

    public int read() throws IOException {
        return in.read();
    }

    public void mark(int readLimit) {
        in.mark(readLimit);
    }
}
