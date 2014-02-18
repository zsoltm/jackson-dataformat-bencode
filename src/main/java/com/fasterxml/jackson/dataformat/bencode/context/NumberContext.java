package com.fasterxml.jackson.dataformat.bencode.context;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.dataformat.bencode.MutableLocation;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

public class NumberContext {
    private InputStream in;
    private MutableLocation mutableLocation;
    public static final int MAX_SUPPORTED_NUMBER_LENGTH = 63; // including possible leading minus (-) sign.
    private byte[] numBuf = new byte[MAX_SUPPORTED_NUMBER_LENGTH + 1]; // length of Long.MIN_VALUE + 1

    public NumberContext(InputStream in, MutableLocation mutableLocation) {
        this.in = in;
        this.mutableLocation = mutableLocation;
    }

    public JsonParser.NumberType guessType() {
        return JsonParser.NumberType.INT; // TODO implement guess
    }

    private int parseNum(int bufStartOffset, byte expectedEnd) throws IOException {
        // TODO update mutableLocation
        in.mark(numBuf.length + 1 - bufStartOffset);
        int read = in.read(numBuf, bufStartOffset, numBuf.length - bufStartOffset) + bufStartOffset;
        if (read < 1) throw new JsonParseException("invalid number", mutableLocation.getJsonLocation(in));
        boolean negative = numBuf[0] == '-';
        int processed = 0;
        if (negative) {
            processed = 1;
            if (read < 2) throw new JsonParseException("invalid number", mutableLocation.getJsonLocation(in));
        }
        in.reset();
        byte token = -1;
        int result = 0;

        while (processed < read && (token = numBuf[processed++]) != expectedEnd) {
            result *= 10;
            if (token >= '0' && token <= '9') {
                token -= '0';
                result += token;
            } else {
                throw new JsonParseException(
                        "unexpected input while parsing a number", mutableLocation.getJsonLocation(in));
            }
        }

        if (token != expectedEnd || token == -1) throw new JsonParseException(
                "invalid number", mutableLocation.getJsonLocation(in));

        return result;
    }

    public int parseInt(byte firstToken, byte expectedEnd) throws IOException {
        numBuf[0] = firstToken;
        return parseNum(1, expectedEnd);
    }

    public int parseInt() {
        return -1; // TODO implement
    }

    public Number parseNumber() {
        return null; // TODO implement
    }

    public long parseLong() {
        return -1l; // TODO implement
    }


    public BigInteger parseBigInteger() {
        return null;  // TODO implement
    }
}
