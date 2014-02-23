package com.fasterxml.jackson.dataformat.bencode.location;

public class MutableLocation extends Location {
    public void advance(int bytes, int chars) {
        inBytes += bytes;
        inChars += chars;
    }

    public void advance(int bytes) {
        inBytes += bytes;
        inChars += bytes;
    }
}
