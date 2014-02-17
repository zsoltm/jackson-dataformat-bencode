package com.fasterxml.jackson.dataformat.bencode;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.base.ParserMinimalBase;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import static com.fasterxml.jackson.dataformat.bencode.PackageVersion.VERSION;

public class BEncodeParser extends ParserMinimalBase {

    private ObjectCodec codec;


    public BEncodeParser(ObjectCodec codec) {
        this.codec = codec;
    }

    @Override
    public Version version() {
        return VERSION;
    }

    @Override
    public JsonToken nextToken() throws IOException, JsonParseException {
        return null;
    }

    @Override
    protected void _handleEOF() throws JsonParseException {

    }

    @Override
    public String getCurrentName() throws IOException, JsonParseException {
        return null;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public JsonStreamContext getParsingContext() {
        return null;
    }

    @Override
    public void overrideCurrentName(String name) {

    }

    @Override
    public String getText() throws IOException, JsonParseException {
        return null;
    }

    @Override
    public char[] getTextCharacters() throws IOException, JsonParseException {
        return new char[0];
    }

    @Override
    public boolean hasTextCharacters() {
        return false;
    }

    @Override
    public int getTextLength() throws IOException, JsonParseException {
        return 0;
    }

    @Override
    public int getTextOffset() throws IOException, JsonParseException {
        return 0;
    }

    @Override
    public byte[] getBinaryValue(Base64Variant b64variant) throws IOException, JsonParseException {
        return new byte[0];
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
        return null;
    }

    @Override
    public JsonLocation getCurrentLocation() {
        return null;
    }

    @Override
    public Number getNumberValue() throws IOException, JsonParseException {
        return null;
    }

    @Override
    public NumberType getNumberType() throws IOException, JsonParseException {
        return null;
    }

    @Override
    public int getIntValue() throws IOException, JsonParseException {
        return 0;
    }

    @Override
    public long getLongValue() throws IOException, JsonParseException {
        return 0;
    }

    @Override
    public BigInteger getBigIntegerValue() throws IOException, JsonParseException {
        return null;
    }

    @Override
    public float getFloatValue() throws IOException, JsonParseException {
        return 0;
    }

    @Override
    public double getDoubleValue() throws IOException, JsonParseException {
        return 0;
    }

    @Override
    public BigDecimal getDecimalValue() throws IOException, JsonParseException {
        return null;
    }

    @Override
    public Object getEmbeddedObject() throws IOException, JsonParseException {
        return null;
    }
}
