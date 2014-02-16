package com.fasterxml.jackson.dataformat.bencode;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.dataformat.bencode.context.IOOutputContext;
import com.fasterxml.jackson.dataformat.bencode.context.WriteContext;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;

/**
 * Writer Based
 */
public class BEncodeGenerator extends JsonGenerator {

    static final byte INTEGER_PREFIX = 'i';
    static final byte LIST_PREFIX = 'l';
    static final byte DICTIONARY_PREFIX = 'd';
    static final byte END_SUFFIX = 'e';
    static final byte STRING_SEPARATOR = ':';
    static final Charset UTF_8 = Charset.forName("UTF-8");

    private final IOOutputContext outputContext;
    private WriteContext writeContext;

    private static final byte[] NULL_VALUE = ("4" + (char) STRING_SEPARATOR + "null").getBytes();
    private static final byte[] TRUE_VALUE = ("4" + (char) STRING_SEPARATOR + "true").getBytes();
    private static final byte[] FALSE_VALUE = ("5" + (char) STRING_SEPARATOR + "false").getBytes();

    public BEncodeGenerator(int features, ObjectCodec codec, IOOutputContext outputContext) {
        writeContext = new WriteContext();
        this.outputContext = outputContext;
//        _outputBuffer = ctxt.allocConcatBuffer();
//        _outputEnd = _outputBuffer.length;
    }


    @Override
    public Object getOutputTarget() {
        return null; // TODO implement
    }

    @Override
    public JsonGenerator setCodec(ObjectCodec oc) {
        return null;
    }

    @Override
    public ObjectCodec getCodec() {
        return null;
    }

    @Override
    public Version version() {
        return null;
    }

    @Override
    public JsonGenerator enable(Feature f) {
        return null;
    }

    @Override
    public JsonGenerator disable(Feature f) {
        return null;
    }

    @Override
    public boolean isEnabled(Feature f) {
        return false;
    }

    @Override
    public int getFeatureMask() {
        return 0;
    }

    @Override
    public JsonGenerator setFeatureMask(int mask) {
        return null;
    }

    @Override
    public JsonGenerator useDefaultPrettyPrinter() {
        return null;
    }

    @Override
    public void writeFieldName(SerializableString name) throws IOException, JsonGenerationException {
        writeFieldName(name.getValue());
    }

    @Override
    public void writeString(SerializableString text) throws IOException, JsonGenerationException {
        writeString(text.getValue());
    }

    @Override
    public void writeRawValue(String text) throws IOException, JsonGenerationException {
        writeString(text);
    }

    @Override
    public void writeRawValue(String text, int offset, int len) throws IOException, JsonGenerationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeRawValue(char[] text, int offset, int len) throws IOException, JsonGenerationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int writeBinary(Base64Variant b64variant, InputStream data, int dataLength) throws IOException, JsonGenerationException {
        return 0;
    }

    @Override
    public void writeObject(Object pojo) throws IOException, JsonProcessingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeTree(TreeNode rootNode) throws IOException, JsonProcessingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public JsonStreamContext getOutputContext() {
        return null;
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public void close() throws IOException {
        outputContext.close();
    }

    @Override
    public void flush() throws IOException {
        outputContext.flush();
    }

    @Override
    public void writeStartArray() throws IOException, JsonGenerationException {
        writeContext.writeValue();
        writeContext = writeContext.createChildList();
        outputContext.write(LIST_PREFIX);
    }

    @Override
    public void writeEndArray() throws IOException, JsonGenerationException {
        if (!writeContext.inList()) throw new JsonGenerationException("not in list");
        writeContext = writeContext.changeToParent();
        outputContext.write(END_SUFFIX);
    }

    @Override
    public void writeStartObject() throws IOException, JsonGenerationException {
        writeContext.writeValue();
        writeContext = writeContext.createChildDictionary();
        outputContext.write(DICTIONARY_PREFIX);
    }

    @Override
    public void writeEndObject() throws IOException, JsonGenerationException {
        if (!writeContext.inDict()) throw new JsonGenerationException("not in dictionary");
        writeContext = writeContext.changeToParent();
        outputContext.write(END_SUFFIX);
    }

    @Override
    public void writeFieldName(String name) throws IOException, JsonGenerationException {
        writeContext.writeKey(name);
        encodeString(name);
    }

    @Override
    public void writeString(String text) throws IOException, JsonGenerationException {
        writeContext.writeValue();
        encodeString(text);
    }

    private void encodeLength(int len) throws IOException {
        outputContext.write(len);
        outputContext.write(STRING_SEPARATOR);
    }

    private void encodeString(String text) throws IOException {
        byte[] bytes = text.getBytes(outputContext.getCharset());
        encodeLength(bytes.length);
        outputContext.write(bytes);
    }

    @Override
    public void writeString(char[] text, int offset, int len) throws IOException, JsonGenerationException {
        writeString(String.valueOf(text, offset, len));
    }

    @Override
    public void writeRawUTF8String(byte[] text, int offset, int length) throws IOException, JsonGenerationException {
        writeContext.writeValue();
        if (outputContext.getCharset().equals(UTF_8)) {
            encodeLength(length);
            outputContext.write(text, offset, length);
        } else {
            byte[] reEncoded = new String(text, offset, length, UTF_8).getBytes();
            encodeLength(reEncoded.length);
            outputContext.write(reEncoded);
        }
    }

    @Override
    public void writeUTF8String(byte[] text, int offset, int length) throws IOException, JsonGenerationException {
        writeRawUTF8String(text, offset, length);
    }

    @Override
    public void writeRaw(String text) throws IOException, JsonGenerationException {
        outputContext.write(text);
    }

    @Override
    public void writeRaw(String text, int offset, int len) throws IOException, JsonGenerationException {
        char[] dst = new char[len];
        text.getChars(offset, offset + len, dst, 0);
        writeRaw(dst, 0, len);
    }

    @Override
    public void writeRaw(char[] text, int offset, int len) throws IOException, JsonGenerationException {
        outputContext.write(text, offset, len);
    }

    @Override
    public void writeRaw(char c) throws IOException, JsonGenerationException {
        outputContext.write(String.valueOf(c));
    }

    @Override
    public void writeBinary(Base64Variant b64variant, byte[] data, int offset, int len) throws IOException, JsonGenerationException {
        writeContext.writeValue();
        encodeLength(len);
        outputContext.write(data, offset, len);
    }

    @Override
    public void writeNumber(int v) throws IOException, JsonGenerationException {
        writeContext.writeValue();
        outputContext.write(INTEGER_PREFIX);
        outputContext.write(v);
        outputContext.write(END_SUFFIX);
    }

    @Override
    public void writeNumber(long v) throws IOException, JsonGenerationException {
        writeContext.writeValue();
        outputContext.write(INTEGER_PREFIX);
        outputContext.write(v);
        outputContext.write(END_SUFFIX);
    }

    @Override
    public void writeNumber(BigInteger v) throws IOException, JsonGenerationException {
        writeContext.writeValue();
        outputContext.write(INTEGER_PREFIX);
        outputContext.write(v.toString());
        outputContext.write(END_SUFFIX);
    }

    @Override
    public void writeNumber(double d) throws IOException, JsonGenerationException {
        throw new UnsupportedOperationException("floating point types are not supported by BEncode specification");
    }

    @Override
    public void writeNumber(float f) throws IOException, JsonGenerationException {
        throw new UnsupportedOperationException("floating point types are not supported by BEncode specification");
    }

    @Override
    public void writeNumber(BigDecimal dec) throws IOException, JsonGenerationException {
        throw new UnsupportedOperationException("floating point types are not supported by BEncode specification");
    }

    @Override
    public void writeNumber(String encodedValue) throws IOException, JsonGenerationException, UnsupportedOperationException {
        writeString(encodedValue);
    }

    @Override
    public void writeBoolean(boolean state) throws IOException, JsonGenerationException {
        writeContext.writeValue();
        outputContext.write(state ? TRUE_VALUE : FALSE_VALUE);
    }

    @Override
    public void writeNull() throws IOException, JsonGenerationException {
        writeContext.writeValue();
        outputContext.write(NULL_VALUE);
    }
}
