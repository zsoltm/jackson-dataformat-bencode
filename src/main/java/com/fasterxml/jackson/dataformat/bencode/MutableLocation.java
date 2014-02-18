package com.fasterxml.jackson.dataformat.bencode;


import com.fasterxml.jackson.core.JsonLocation;

public class MutableLocation {
    private int inBytes = 0;
    private int inChars = 0;

    public MutableLocation() {
        inBytes = 0;
        inChars = 0;
    }

    protected MutableLocation(MutableLocation location) {
        this.inBytes = location.inBytes;
        this.inChars = location.inChars;
    }

    public static MutableLocation newInstance(MutableLocation location) {
        return new MutableLocation(location);
    }

    public MutableLocation newInstance() {
        return newInstance(this);
    }

    public void advance(int bytes, int chars) {
        inBytes += bytes;
        inChars += chars;
    }

    public void advance(int bytes) {
        inBytes += bytes;
        inChars += bytes;
    }

    public JsonLocation getJsonLocation(Object objectRef) {
        return new JsonLocation(objectRef, inBytes, inChars, 1, inChars);
    }
}
