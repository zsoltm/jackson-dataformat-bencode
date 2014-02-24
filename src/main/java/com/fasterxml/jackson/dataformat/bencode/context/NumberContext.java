package com.fasterxml.jackson.dataformat.bencode.context;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;

public class NumberContext {
    private static final long[] TEN_TBL = {10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000};
    private final StreamInputContext sic;

    /** including possible leading minus (-) sign, should be >= 20. */
    public static final int MAX_SUPPORTED_NUMBER_LENGTH = 63; //

    private byte[] numBuf = new byte[MAX_SUPPORTED_NUMBER_LENGTH + 1]; // +1 to be able to catch expected terminator

    private static byte[] MAX_LONG_STR = "9223372036854775807".getBytes(Charset.forName("ISO-8859-1"));
    private static byte[] MAX_INT_STR = "2147483647".getBytes(Charset.forName("ISO-8859-1"));
    private static byte[] MIN_LONG_STR = "-9223372036854775808".getBytes(Charset.forName("ISO-8859-1"));
    private static byte[] MIN_INT_STR = "-2147483648".getBytes(Charset.forName("ISO-8859-1"));

    private int numberLength;
    private JsonParser.NumberType currentType;
    private int currentPtr;
    private boolean currentNegative;

    public NumberContext(StreamInputContext sic) {
        this.sic = sic;
        resetCurrentGuess();
    }

    public final boolean isLatinDigit(byte c) {
        return c >= '0' && c <= '9';
    }

    private int determineNumberLength(int startOffset, int length) throws JsonParseException {
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

    int compareBytes(byte[] a, int offset) {
        for (byte ca : a) {
            if (ca < numBuf[offset]) return -1;
            if (ca > numBuf[offset++]) return 1;
        }
        return 0;
    }

    public JsonParser.NumberType guessType() throws IOException {
        // only int -> long -> BigInt need to be handled
        final int readBytes = MAX_SUPPORTED_NUMBER_LENGTH + 1;
        sic.mark(readBytes);

        try {
            numberLength = determineNumberLength(0, sic.read(numBuf, 0, readBytes));
            if (numberLength == 0) throw new JsonParseException(
                    "tried to guess number with insufficient input available", sic.getJsonLocation());

            currentNegative = numBuf[0] == '-';
            currentPtr = currentNegative ? 1 : 0;
            final byte[] int_str = currentNegative ? MIN_INT_STR : MAX_INT_STR;
            final byte[] long_str = currentNegative ? MIN_LONG_STR : MAX_LONG_STR;

            if (numberLength < int_str.length) {
                return (currentType = JsonParser.NumberType.INT);
            }

            if (numberLength < long_str.length) {
                return (currentType = numberLength == int_str.length && compareBytes(int_str, 0) >= 0 ?
                        JsonParser.NumberType.INT : JsonParser.NumberType.LONG);
            }

            return (currentType = numberLength == long_str.length && compareBytes(long_str, 0) >= 0 ?
                    JsonParser.NumberType.LONG : JsonParser.NumberType.BIG_INTEGER);
        } finally {
            sic.reset();
        }
    }

    private void ensureGuessPerformedFor(JsonParser.NumberType expectedType) {
        if (numberLength < 0) throw new IllegalStateException("number size should be guessed before parse");
        if (currentType.ordinal() > expectedType.ordinal()) throw new IllegalStateException("integer overflow");
    }

    private void resetCurrentGuess() {
        numberLength = -1;
    }

    private int determineEnd(JsonParser.NumberType expectedTye, int positiveLen, int negativeLen) {
        return Math.min(
                currentPtr + (currentNegative ? negativeLen - 1 : positiveLen) - (currentType == expectedTye ? 0 : 1),
                numberLength);
    }

    private int parseIntInternal() {
        int end = determineEnd(JsonParser.NumberType.INT, MAX_INT_STR.length, MIN_INT_STR.length);
        int value = 0;

        for (int i = currentPtr; i < end; i++) {
            value *= 10;
            value += numBuf[i] - '0';
        }

        currentPtr = end;
        return value;
    }

    private long parseLongInternal() {
        int end = determineEnd(JsonParser.NumberType.LONG, MAX_LONG_STR.length, MIN_LONG_STR.length);
        long value = 0;
        int nextValue;
        int prevCurrentPtr;

        while (currentPtr < end) {
            prevCurrentPtr = currentPtr;
            nextValue = parseIntInternal();
            if (value > 0) value *= TEN_TBL[currentPtr - prevCurrentPtr - 1];
            value += nextValue;
        }

        currentPtr = end;
        return value;
    }

    private BigInteger parseBigIntegerInternal() {
        int end = determineEnd(
                JsonParser.NumberType.BIG_INTEGER, MAX_SUPPORTED_NUMBER_LENGTH, MAX_SUPPORTED_NUMBER_LENGTH);
        BigInteger value = BigInteger.ZERO;
        int nextValue;
        int prevCurrentPtr;

        while (currentPtr < end) {
            prevCurrentPtr = currentPtr;
            nextValue = parseIntInternal();
            if (!value.equals(BigInteger.ZERO)) value = value.multiply(BigInteger.TEN.pow(currentPtr - prevCurrentPtr));
            value = value.add(BigInteger.valueOf(nextValue));
        }

        currentPtr = end;
        return value;
    }

    public int parseInt() throws IOException {
        ensureGuessPerformedFor(JsonParser.NumberType.INT);
        int value = parseIntInternal();
        //noinspection ResultOfMethodCallIgnored
        sic.skip(currentPtr);
        resetCurrentGuess();
        return currentNegative && value > 0 ? -value : value;
    }

    public long parseLong() throws IOException {
        ensureGuessPerformedFor(JsonParser.NumberType.LONG);
        long value = parseLongInternal();
        //noinspection ResultOfMethodCallIgnored
        sic.skip(currentPtr);
        resetCurrentGuess();
        return currentNegative && value > 0 ? -value : value;

    }

    public BigInteger parseBigInteger() throws IOException {
        ensureGuessPerformedFor(JsonParser.NumberType.BIG_INTEGER);
        BigInteger value = parseBigIntegerInternal();
        //noinspection ResultOfMethodCallIgnored
        sic.skip(currentPtr);
        resetCurrentGuess();
        return value;
    }

    public Number parseNumber() {
        throw new NotImplementedException(); // TODO
    }
}
