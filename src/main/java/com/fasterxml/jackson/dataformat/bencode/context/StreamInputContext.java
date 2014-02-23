package com.fasterxml.jackson.dataformat.bencode.context;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.dataformat.bencode.location.Location;
import com.fasterxml.jackson.dataformat.bencode.location.MutableLocation;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamInputContext extends InputStream {
    private final InputStream in;
    private final InputStream rawIn;
    private MutableLocation location, markedLocation;
    private boolean marked = false;

    public StreamInputContext(InputStream in) {
        this.rawIn = in;
        this.in = in.markSupported() ? in : new BufferedInputStream(in);
        location = new MutableLocation();
        markedLocation = new MutableLocation();
    }

    @Override
    public long skip(long n) throws IOException {
        long skipped = 0, cSkipped;
        while (skipped < n && (cSkipped = in.skip(n - skipped)) > 0) skipped += cSkipped;
        location.advance((int) skipped);
        return skipped;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    @Override
    public void reset() throws IOException {
        if (!marked) throw new IllegalStateException("reset without preceding mark");
        marked = false;
        location.set(markedLocation);
        in.reset();
    }

    @Override
    public int read(byte[] bytes, int off, int len) throws IOException {
        int sumLen = 0, currentLen;
        while ((sumLen < len) && (currentLen = in.read(bytes, off + sumLen, len - sumLen)) > 0) sumLen += currentLen;
        location.advance(sumLen);
        return sumLen;
    }

    @Override
    public int read() throws IOException {
        location.advance(1);
        return in.read();
    }

    @Override
    public void mark(int readLimit) {
        marked = true;
        markedLocation.set(location);
        in.mark(readLimit);
    }

    @Override
    public int available() throws IOException {
        return rawIn.available();
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    public JsonLocation getJsonLocation() {
        return location.getJsonLocation(rawIn);
    }

    public Location getLocation() {
        return location;
    }
}
