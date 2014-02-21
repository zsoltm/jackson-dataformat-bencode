package com.fasterxml.jackson.dataformat.bencode.context;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.dataformat.bencode.MutableLocation;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;

public class NumberContext {
    private final StreamInputContext sic;
    private MutableLocation mutableLocation;

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

    public NumberContext(StreamInputContext sic, MutableLocation mutableLocation) {
        this.sic = sic;
        this.mutableLocation = mutableLocation;
        resetCurrentGuess();
    }

    public final boolean isLatinDigit(byte c) {
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
        sic.mark(readBytes);

        try {
            numberLength = determineNumberLength(0, sic.read(numBuf, 0, readBytes));
            if (numberLength == 0) throw new JsonParseException(
                    "tried to guess number with insufficient input available", mutableLocation.getJsonLocation(null));

            currentPtr = numberLength;
            currentNegative = numBuf[0] == '-';
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

    private void ensureGuessPerformed() {
        if (numberLength < 0) throw new IllegalStateException("number size should be guessed before parse");
    }

    private void resetCurrentGuess() {
        numberLength = -1;
    }

    private int determineParseLength(JsonParser.NumberType expectedTye, int positiveLen, int negativeLen) {
        return currentType == expectedTye ?
                (currentNegative ? 1 : 0) :
                (currentNegative ?
                        Math.max(numberLength - negativeLen, 0) + 2 :
                        Math.max(numberLength - positiveLen, 0) + 1);
    }

    private int parseIntInternal() {
        int endIndex = determineParseLength(JsonParser.NumberType.INT, MAX_INT_STR.length, MIN_INT_STR.length);
        int value = 0;

        while (currentPtr-- > endIndex) {
            value *= 10;
            value += numBuf[currentPtr] - '0';
        }

        return value;
    }

    private long parseLongInternal(int carry) {
        int endIndex = determineParseLength(JsonParser.NumberType.LONG, MAX_LONG_STR.length, MIN_INT_STR.length);
        long value = carry;

        while (currentPtr-- > endIndex) {
            value *= 10;
            value += numBuf[currentPtr] - '0';
        }

        return value;
    }

    private BigInteger parseBigIntegerInternal(long carry) {
        int endIndex = determineParseLength(JsonParser.NumberType.LONG, MAX_LONG_STR.length, MIN_INT_STR.length);
        BigInteger value = BigInteger.valueOf(carry);

        while (currentPtr-- > endIndex) {
            value = value.multiply(BigInteger.TEN);
            value = value.add(BigInteger.valueOf(numBuf[currentPtr] - '0'));
        }

        return value;
    }

    public int parseInt() throws JsonParseException {
        ensureGuessPerformed();
        if (currentType != JsonParser.NumberType.INT) throw new IllegalStateException("type mismatch");
        int value = parseIntInternal();
        resetCurrentGuess();
        return currentNegative && value > 0 ? -value : value;
    }

    public long parseLong() {
        ensureGuessPerformed();
        if (currentType != JsonParser.NumberType.LONG) throw new IllegalStateException("type mismatch");
        long value = parseLongInternal(parseIntInternal());
        resetCurrentGuess();
        return currentNegative && value > 0 ? -value : value;

    }

    public BigInteger parseBigInteger() {
        ensureGuessPerformed();
        if (currentType != JsonParser.NumberType.BIG_INTEGER) throw new IllegalStateException("type mismatch");
        BigInteger value = parseBigIntegerInternal(parseLongInternal(parseIntInternal()));
        resetCurrentGuess();
        return value;
    }

    public Number parseNumber() {
        return null; // TODO implement
    }
}
