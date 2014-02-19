package com.fasterxml.jackson.dataformat.bencode.context;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.dataformat.bencode.MutableLocation;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;

public class NumberContext {
    private InputStream in;
    private MutableLocation mutableLocation;

    /** including possible leading minus (-) sign, should be >= 20. */
    public static final int MAX_SUPPORTED_NUMBER_LENGTH = 63; //

    private byte[] numBuf = new byte[MAX_SUPPORTED_NUMBER_LENGTH + 1]; // +1 to be able to catch expected terminator

    private static byte[] MAX_LONG_STR = "9223372036854775807".getBytes(Charset.forName("ISO-8859-1"));
    private static byte[] MAX_INT_STR = "2147483647".getBytes(Charset.forName("ISO-8859-1"));
    private static byte[] MIN_LONG_STR = "-9223372036854775808".getBytes(Charset.forName("ISO-8859-1"));
    private static byte[] MIN_INT_STR = "-2147483648".getBytes(Charset.forName("ISO-8859-1"));

    public NumberContext(InputStream in, MutableLocation mutableLocation) {
        this.in = in;
        this.mutableLocation = mutableLocation;
    }

    final boolean isLatinDigit(byte c) {
        return c >= '0' && c <= '9';
    }

    int determineNumberLength(int startOffset, int length) throws JsonParseException {
        int offset = startOffset;
        boolean negative = length > 0 && numBuf[offset] == '-';
        if (negative) offset++;
        length += offset;

        while (offset < length && isLatinDigit(numBuf[offset])) {
            offset++;
        }

        final int numberLength = offset - startOffset;
        if (negative && numberLength == 1) return 0;

        return numberLength;
    }

    int tryReadN(int offset, int length) throws IOException {
        int sumLen = 0, currentLen;

        while ((sumLen < length) && (currentLen = in.read(numBuf, offset + sumLen, length - sumLen)) > 0) {
            sumLen += currentLen;
        }

        return sumLen;
    }

    int compareBytes(byte[] a, int offset) {
        for (byte ca : a) {
            if (ca < numBuf[offset]) return -1;
            if (ca > numBuf[offset++]) return 1;
        }
        return 0;
    }

    public JsonParser.NumberType guessType() throws IOException {
        // only int -> long -> BigInt need to be handled
        final int readBytes = MIN_LONG_STR.length + 1;
        int numberLength = 0;
        in.mark(readBytes); // TODO avoid mark / skip by using a shared input context!

        try {
            numberLength = determineNumberLength(0, tryReadN(0, readBytes));
            if (numberLength == 0) throw new JsonParseException(
                    "tried to guess number with insufficient input available", mutableLocation.getJsonLocation(in));

            final boolean negative = numBuf[0] == '-';
            final byte[] int_str = negative ? MIN_INT_STR : MAX_INT_STR;
            final byte[] long_str = negative ? MIN_LONG_STR : MAX_LONG_STR;

            if (numberLength < int_str.length) {
                return JsonParser.NumberType.INT;
            }

            if (numberLength < long_str.length) {
                return numberLength == int_str.length && compareBytes(int_str, 0) >= 0 ?
                        JsonParser.NumberType.INT : JsonParser.NumberType.LONG;
            }

            return numberLength == long_str.length && compareBytes(long_str, 0) >= 0 ?
                    JsonParser.NumberType.LONG : JsonParser.NumberType.BIG_INTEGER;
        } finally {
            in.reset();
            in.skip(numberLength);
        }
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
