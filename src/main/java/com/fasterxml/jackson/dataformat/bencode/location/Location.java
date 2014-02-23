package com.fasterxml.jackson.dataformat.bencode.location;


import com.fasterxml.jackson.core.JsonLocation;

public class Location {
    int inBytes = 0;
    int inChars = 0;

    public void set(Location location) {
        this.inBytes = location.inBytes;
        this.inChars = location.inChars;
    }

    public JsonLocation getJsonLocation(Object objectRef) {
        return new JsonLocation(objectRef, inBytes, inChars, 1, inChars);
    }
}
