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
import com.fasterxml.jackson.dataformat.bencode.location.Location;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
    private StreamInputContext sic;
    private boolean closed = false;
    private BContext ctx = new BContext();
    private int nextStringLength = -1;
    private NumberContext numberContext;
    private Location lastTokenLocation = new Location();

    public BEncodeParser(InputStream in, ObjectCodec codec) {
        this.codec = codec;
        sic = new StreamInputContext(in);
        numberContext = new NumberContext(sic);
    }

    @Override
    public Version version() {
        return VERSION;
    }

    @Override
    public Object getInputSource() {
        return sic;
    }

    @Override
    public JsonToken nextToken() throws IOException {
        lastTokenLocation.set(sic.getLocation());
        sic.mark(2);
        final int token = sic.read();
        final int next = sic.read();
        sic.reset();

        if (token == -1) {
            return (_currToken = JsonToken.NOT_AVAILABLE);
        }

        switch (token) {
            case DICTIONARY_PREFIX:
                ctx.valueNext();
                ctx = ctx.createChildDictionary();
                //noinspection ResultOfMethodCallIgnored
                sic.skip(1);
                _currToken = ctx.getStartToken();
                break;
            case LIST_PREFIX:
                ctx.valueNext();
                ctx = ctx.createChildList();
                //noinspection ResultOfMethodCallIgnored
                sic.skip(1);
                _currToken = ctx.getStartToken();
                break;
            case END_SUFFIX:
                _currToken = ctx.getEndToken();
                ctx = ctx.changeToParent();
                //noinspection ResultOfMethodCallIgnored
                sic.skip(1);
                break;
            case INTEGER_PREFIX:
                //noinspection ResultOfMethodCallIgnored
                sic.skip(1);
                numberContext.guessType();
                _currToken = JsonToken.VALUE_NUMBER_INT;
                break;
            default:
                parseNextLength(token);
                // perform read-ahead for FIELD_NAME due to strange deserializer contract
                if (ctx.getExpected() == BContext.Expect.KEY && next != END_SUFFIX) {
                    //noinspection ResultOfMethodCallIgnored
                    _currToken = JsonToken.FIELD_NAME;
                    getText();
                } else {
                    _currToken = JsonToken.VALUE_STRING;
                }
        }

        return _currToken;
    }

    protected void parseNextLength(int token) throws IOException {
        if (token < '0' && token > '9') throw new JsonParseException("unknown token", getCurrentLocation());
        if (numberContext.guessType() != NumberType.INT)
            throw new JsonParseException("size overflow", getCurrentLocation());
        nextStringLength = numberContext.parseInt();
        if (nextStringLength < 0)
            throw new JsonParseException("illegal byte string size", getCurrentLocation());
        token = sic.read();
        if (token != ':')
            throw new JsonParseException("malformed byte string length token", getCurrentLocation());
    }

    @Override
    protected void _handleEOF() throws JsonParseException {
        if (!ctx.inRoot()) {
            throw new JsonParseException("EOF while not sic root context", getCurrentLocation());
        }
    }

    @Override
    public String getCurrentName() throws IOException {
        return ctx.getCurrentName();
    }

    @Override
    public void close() throws IOException {
        closed = true;
        sic.close();
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
        throw new UnsupportedOperationException("please do not override names");
    }

    @Override
    public String getText() throws IOException {
        String returnValue = new String(getBinaryInternal(), UTF_8); // TODO add encoding support support
        if (_currToken == JsonToken.FIELD_NAME) {
            ctx.keyNext(returnValue);
        } else ctx.valueNext();

        return returnValue;
    }

    @Override
    public char[] getTextCharacters() throws IOException {
        throw new NotImplementedException(); // hasTextCharacters is always false
    }

    @Override
    public boolean hasTextCharacters() {
        return false;
    }

    @Override
    public int getTextLength() throws IOException {
        throw new UnsupportedOperationException("using direct character buffer access is not supported");
    }

    @Override
    public int getTextOffset() throws IOException {
        throw new UnsupportedOperationException("using direct caharacter buffer access is not supported");
    }

    @Override
    public byte[] getBinaryValue(Base64Variant b64variant) throws IOException {
        ctx.valueNext();
        return getBinaryInternal();
    }

    private byte[] getBinaryInternal() throws IOException {
        if (nextStringLength < 0)
            throw new IllegalStateException("next token should be determined before invoking getText");

        byte[] bytes = new byte[nextStringLength];
        int readLen;

        readLen = sic.read(bytes, 0, nextStringLength);
        if (readLen < nextStringLength) throw new JsonParseException("unexpected EOF", getCurrentLocation());
        nextStringLength = -1;
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
        return lastTokenLocation.getJsonLocation(sic);
    }

    @Override
    public JsonLocation getCurrentLocation() {
        return sic.getJsonLocation();
    }

    @Override
    public NumberType getNumberType() throws IOException {
        return numberContext.guessType();
    }

    @Override
    public Number getNumberValue() throws IOException {
        ctx.valueNext();
        Number n = numberContext.parseNumber();
        checkIntegerIsClosed();
        return n;
    }

    @Override
    public int getIntValue() throws IOException {
        ctx.valueNext();
        int value = numberContext.parseInt();
        checkIntegerIsClosed();
        return value;
    }

    @Override
    public long getLongValue() throws IOException {
        ctx.valueNext();
        long value = numberContext.parseLong();
        checkIntegerIsClosed();
        return value;
    }

    @Override
    public BigInteger getBigIntegerValue() throws IOException {
        ctx.valueNext();
        BigInteger value = numberContext.parseBigInteger();
        checkIntegerIsClosed();
        return value;
    }

    private void checkIntegerIsClosed() throws IOException {
        if (sic.read() != 'e') throw new JsonParseException("integer not closed", sic.getJsonLocation());
    }

    @Override
    public float getFloatValue() throws IOException {
        ctx.valueNext();
        throw new UnsupportedOperationException("BEncode does not support float values");
    }

    @Override
    public double getDoubleValue() throws IOException {
        ctx.valueNext();
        throw new UnsupportedOperationException("BEncode does not support double values");
    }

    @Override
    public BigDecimal getDecimalValue() throws IOException {
        ctx.valueNext();
        throw new UnsupportedOperationException("BEncode does not support decimal values");
    }

    @Override
    public Object getEmbeddedObject() throws IOException {
        ctx.valueNext();
        throw new UnsupportedOperationException("BEncode does not support embedded objects");
    }
}
