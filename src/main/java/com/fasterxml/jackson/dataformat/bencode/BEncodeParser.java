package com.fasterxml.jackson.dataformat.bencode;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.base.ParserMinimalBase;
import com.fasterxml.jackson.dataformat.bencode.context.BContext;
import com.fasterxml.jackson.dataformat.bencode.context.NumberContext;
import com.fasterxml.jackson.dataformat.bencode.context.StreamInputContext;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

import static com.fasterxml.jackson.dataformat.bencode.BEncodeFormat.DICTIONARY_PREFIX;
import static com.fasterxml.jackson.dataformat.bencode.BEncodeFormat.END_SUFFIX;
import static com.fasterxml.jackson.dataformat.bencode.BEncodeFormat.INTEGER_PREFIX;
import static com.fasterxml.jackson.dataformat.bencode.BEncodeFormat.LIST_PREFIX;
import static com.fasterxml.jackson.dataformat.bencode.BEncodeFormat.UTF_8;
import static com.fasterxml.jackson.dataformat.bencode.PackageVersion.VERSION;

public class BEncodeParser extends ParserMinimalBase {

    private ObjectCodec codec;
    private StreamInputContext in;
    private boolean closed = false;
    private BContext ctx = new BContext();
    private int nextStringLength;
    private NumberContext numberContext;
    private MutableLocation mutableLocation;
    private MutableLocation lastTokenLocation;

    public BEncodeParser(InputStream in, ObjectCodec codec) {
        this.codec = codec;
        this.in = new StreamInputContext(in.markSupported() ? in : new BufferedInputStream(in));
        mutableLocation = new MutableLocation();
        numberContext = new NumberContext(this.in, mutableLocation);
        lastTokenLocation = mutableLocation.newInstance();
    }

    @Override
    public Version version() {
        return VERSION;
    }

    @Override
    public JsonToken nextToken() throws IOException {
        in.mark(1);
        int token = in.read();
        in.reset();
        lastTokenLocation = mutableLocation.newInstance();

        if (token == -1) {
            return JsonToken.NOT_AVAILABLE; // TODO handle EOF
        }

        try {
            JsonToken returnToken;

            switch (token) {
                case DICTIONARY_PREFIX:
                    ctx.valueNext();
                    ctx = ctx.createChildDictionary();
                    in.skip(1);
                    return ctx.getStartToken();
                case LIST_PREFIX:
                    ctx.valueNext();
                    ctx = ctx.createChildList();
                    in.skip(1);
                    return ctx.getStartToken();
                case END_SUFFIX:
                    returnToken = ctx.getEndToken();
                    ctx = ctx.changeToParent();
                    in.skip(1);
                    return returnToken;
                case INTEGER_PREFIX:
                    ctx.valueNext();
                    in.skip(1);
                    return JsonToken.VALUE_NUMBER_INT;
            }


            if (token >= '0' && token <= '9') {
                if (numberContext.guessType() != NumberType.INT)
                    throw new JsonParseException("size overflow", getCurrentLocation());
                nextStringLength = numberContext.parseInt();
                if (nextStringLength < 0)
                    throw new JsonParseException("illegal byte string size", getCurrentLocation());
                if (ctx.getExpected() == BContext.Expect.KEY) {
                    returnToken = JsonToken.FIELD_NAME;
                } else {
                    ctx.valueNext();
                    returnToken = JsonToken.VALUE_STRING;
                }

                return returnToken;
            }

            throw new JsonParseException("unknown token", getCurrentLocation());
        } finally {
            mutableLocation.advance(1);
        }
    }

    @Override
    protected void _handleEOF() throws JsonParseException {
        // TODO implement?
    }

    @Override
    public String getCurrentName() throws IOException {
        return null;
    }

    @Override
    public void close() throws IOException {
        closed = true;
        in.close();
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public JsonStreamContext getParsingContext() {
        return null;
    }

    @Override
    public void overrideCurrentName(String name) {
        // TODO implement
    }

    @Override
    public String getText() throws IOException {
        String returnValue = new String(getBinaryValue(), 0, nextStringLength, UTF_8);
        if (_currToken == JsonToken.FIELD_NAME) {
            ctx.keyNext(returnValue);
        }
        return returnValue;
    }

    @Override
    public char[] getTextCharacters() throws IOException {
        String value = getText();
        int len = value.length();
        char[] ch = new char[len];
        value.getChars(0, len, ch, 0);
        return ch;
    }

    @Override
    public boolean hasTextCharacters() {
        return false; // TODO implement
    }

    @Override
    public int getTextLength() throws IOException {
        return 0; // TODO implement
    }

    @Override
    public int getTextOffset() throws IOException {
        return 0; // TODO implement
    }

    @Override
    public byte[] getBinaryValue(Base64Variant b64variant) throws IOException {
        byte[] bytes = new byte[nextStringLength];
        int readTotal = 0;
        int readLen;

        while (readTotal < nextStringLength) {
            readLen = in.read(bytes, 0, nextStringLength);
            if (readLen <= 0) throw new JsonParseException("unexpected EOF", getCurrentLocation());
            readTotal += readLen;
        }

        return bytes;
    }

    @Override
    public ObjectCodec getCodec() {
        return codec;
    }

    @Override
    public void setCodec(ObjectCodec c) {
        this.codec = c;
    }

    @Override
    public JsonLocation getTokenLocation() {
        return lastTokenLocation.getJsonLocation(in);
    }

    @Override
    public JsonLocation getCurrentLocation() {
        return mutableLocation.getJsonLocation(in);
    }

    @Override
    public Number getNumberValue() throws IOException {
        return numberContext.parseNumber();
    }

    @Override
    public NumberType getNumberType() throws IOException {
        return numberContext.guessType();
    }

    @Override
    public int getIntValue() throws IOException {
        return numberContext.parseInt();
    }

    @Override
    public long getLongValue() throws IOException {
        return numberContext.parseLong();
    }

    @Override
    public BigInteger getBigIntegerValue() throws IOException {
        return numberContext.parseBigInteger();
    }

    @Override
    public float getFloatValue() throws IOException {
        throw new UnsupportedOperationException("BEncode does not support float values");
    }

    @Override
    public double getDoubleValue() throws IOException {
        throw new UnsupportedOperationException("BEncode does not support double values");
    }

    @Override
    public BigDecimal getDecimalValue() throws IOException {
        throw new UnsupportedOperationException("BEncode does not support decimal values");
    }

    @Override
    public Object getEmbeddedObject() throws IOException {
        return null;
    }
}
