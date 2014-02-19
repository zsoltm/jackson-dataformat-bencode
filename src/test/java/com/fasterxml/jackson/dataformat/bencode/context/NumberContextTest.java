package com.fasterxml.jackson.dataformat.bencode.context;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.dataformat.bencode.MutableLocation;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class NumberContextTest {
    public static final Charset LATIN_1 = Charset.forName("ISO-8859-1");

    @Test
    public void testDetermineNumberLength() throws Exception {
        NumberContext numberContext = createNumberContext("--1234567890asd", true);

        assertThat(numberContext.determineNumberLength(2, 10), is(10));
        assertThat(numberContext.determineNumberLength(1, 11), is(11)); // negative
        assertThat(numberContext.determineNumberLength(3, 9), is(9));
        assertThat(numberContext.determineNumberLength(3, 10), is(9));
        assertThat(numberContext.determineNumberLength(2, 13), is(10));
        assertThat(numberContext.determineNumberLength(1, 14), is(11));
        assertThat(numberContext.determineNumberLength(0, 14), is(0));
        assertThat(numberContext.determineNumberLength(12, 3), is(0));
    }

    @Test
    public void testDetermineNumberLengthOnInsufficientInput() throws Exception {
        NumberContext numberContext = createNumberContext("-1", true);

        try {
            numberContext.determineNumberLength(0, 1);
            fail("should throw if no digits after \"-\" sign");
        } catch (JsonParseException e) {
            assertThat(e.getMessage(), is("tried to guess number with insufficient input available"));
        }

        try {
            numberContext.determineNumberLength(1, 0);
            fail("should throw if no input available");
        } catch (JsonParseException e) {
            assertThat(e.getMessage(), is("tried to guess number with insufficient input available"));
        }

        assertThat(numberContext.determineNumberLength(0, 1), is(-1));
    }

    @Test
    public void testGuessType() throws Exception {
        NumberContext numberContext = createNumberContext("2147483647", false);
        assertThat(numberContext.guessType(), is(JsonParser.NumberType.INT));

        numberContext = createNumberContext("-2147483648", false);
        assertThat(numberContext.guessType(), is(JsonParser.NumberType.INT));

        numberContext = createNumberContext("2147483648", false);
        assertThat(numberContext.guessType(), is(JsonParser.NumberType.LONG));

        numberContext = createNumberContext("-2147483649", false);
        assertThat(numberContext.guessType(), is(JsonParser.NumberType.LONG));

        numberContext = createNumberContext("9223372036854775807xz", false);
        assertThat(numberContext.guessType(), is(JsonParser.NumberType.LONG));

        numberContext = createNumberContext("-9223372036854775808", false);
        assertThat(numberContext.guessType(), is(JsonParser.NumberType.LONG));

        numberContext = createNumberContext("9223372036854775808", false);
        assertThat(numberContext.guessType(), is(JsonParser.NumberType.BIG_INTEGER));

        numberContext = createNumberContext("-9223372036854775809", false);
        assertThat(numberContext.guessType(), is(JsonParser.NumberType.BIG_INTEGER));

        numberContext = createNumberContext("-1", false);
        assertThat(numberContext.guessType(), is(JsonParser.NumberType.INT));
    }

    @Test
    public void testCompareBytes() throws Exception {
        NumberContext numberContext = createNumberContext("1234567890", true);

        assertThat(numberContext.compareBytes("1234567890".getBytes(LATIN_1), 0), is(0));
        assertThat(numberContext.compareBytes("2234567890".getBytes(LATIN_1), 0), is(1));
        assertThat(numberContext.compareBytes("1233567890".getBytes(LATIN_1), 0), is(-1));
        assertThat(numberContext.compareBytes("123456789".getBytes(LATIN_1), 0), is(0));
        assertThat(numberContext.compareBytes("23456789".getBytes(LATIN_1), 1), is(0));
        assertThat(numberContext.compareBytes("23456799".getBytes(LATIN_1), 1), is(1));
        assertThat(numberContext.compareBytes("23356799".getBytes(LATIN_1), 1), is(-1));
    }

    NumberContext createNumberContext(String input, boolean preRead) throws Exception {
        NumberContext numberContext = new NumberContext(
                new ByteArrayInputStream(input.getBytes(LATIN_1)),
                new MutableLocation());

        if (preRead) numberContext.tryReadN(0, input.length()); // ~

        return numberContext;
    }
}
