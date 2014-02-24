package com.fasterxml.jackson.dataformat.bencode;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.dataformat.bencode.context.BContext;
import com.fasterxml.jackson.dataformat.bencode.context.StreamOutputContext;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

import static com.fasterxml.jackson.dataformat.bencode.BEncodeFormat.DICTIONARY_PREFIX;
import static com.fasterxml.jackson.dataformat.bencode.BEncodeFormat.END_SUFFIX;
import static com.fasterxml.jackson.dataformat.bencode.BEncodeFormat.INTEGER_PREFIX;
import static com.fasterxml.jackson.dataformat.bencode.BEncodeFormat.LIST_PREFIX;
import static com.fasterxml.jackson.dataformat.bencode.BEncodeFormat.STRING_SEPARATOR;
import static com.fasterxml.jackson.dataformat.bencode.BEncodeFormat.UTF_8;

/**
 * Writer Based
 */
public class BEncodeGenerator extends JsonGenerator {
    private final StreamOutputContext outputContext;
    private BContext ctx;

    private static final byte[] NULL_VALUE = ("4" + (char) STRING_SEPARATOR + "null").getBytes();
    private static final byte[] TRUE_VALUE = ("4" + (char) STRING_SEPARATOR + "true").getBytes();
    private static final byte[] FALSE_VALUE = ("5" + (char) STRING_SEPARATOR + "false").getBytes();

    public BEncodeGenerator(int features, ObjectCodec codec, StreamOutputContext outputContext) {
        ctx = new BContext();
        this.outputContext = outputContext;
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
    public void writeFieldName(SerializableString name) throws IOException {
        writeFieldName(name.getValue());
    }

    @Override
    public void writeString(SerializableString text) throws IOException {
        writeString(text.getValue());
    }

    @Override
    public void writeRawValue(String text) throws IOException {
        writeString(text);
    }

    @Override
    public void writeRawValue(String text, int offset, int len) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeRawValue(char[] text, int offset, int len) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int writeBinary(Base64Variant b64variant, InputStream data, int dataLength) throws IOException {
        return 0;
    }

    @Override
    public void writeObject(Object pojo) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeTree(TreeNode rootNode) throws IOException {
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
    public void writeStartArray() throws IOException {
        valueNext();
        ctx = ctx.createChildList();
        outputContext.write(LIST_PREFIX);
    }

    @Override
    public void writeEndArray() throws IOException {
        if (!ctx.inArray()) throw new JsonGenerationException("not in list");
        switchToParent();
        outputContext.write(END_SUFFIX);
    }

    @Override
    public void writeStartObject() throws IOException {
        valueNext();
        ctx = ctx.createChildDictionary();
        outputContext.write(DICTIONARY_PREFIX);
    }

    @Override
    public void writeEndObject() throws IOException {
        if (!ctx.inObject()) throw new JsonGenerationException("not in dictionary");
        switchToParent();
        outputContext.write(END_SUFFIX);
    }

    private void switchToParent() throws JsonGenerationException {
        try {
            ctx = ctx.changeToParent();
        } catch (IOException e) {
            throw new JsonGenerationException(e.getMessage());
        }
    }

    @Override
    public void writeFieldName(String name) throws IOException {
        try {
            ctx.keyNext(name);
        } catch (IOException e) {
            throw new JsonGenerationException(e.getMessage());
        }
        encodeString(name);
    }

    @Override
    public void writeString(String text) throws IOException {
        valueNext();
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
    public void writeString(char[] text, int offset, int len) throws IOException {
        writeString(String.valueOf(text, offset, len));
    }

    @Override
    public void writeRawUTF8String(byte[] text, int offset, int length) throws IOException {
        valueNext();
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
    public void writeUTF8String(byte[] text, int offset, int length) throws IOException {
        writeRawUTF8String(text, offset, length);
    }

    @Override
    public void writeRaw(String text) throws IOException {
        outputContext.write(text);
    }

    @Override
    public void writeRaw(String text, int offset, int len) throws IOException {
        char[] dst = new char[len];
        text.getChars(offset, offset + len, dst, 0);
        writeRaw(dst, 0, len);
    }

    @Override
    public void writeRaw(char[] text, int offset, int len) throws IOException {
        outputContext.write(text, offset, len);
    }

    @Override
    public void writeRaw(char c) throws IOException {
        outputContext.write(String.valueOf(c));
    }

    @Override
    public void writeBinary(Base64Variant b64variant, byte[] data, int offset, int len) throws IOException {
        valueNext();
        encodeLength(len);
        outputContext.write(data, offset, len);
    }

    @Override
    public void writeNumber(int v) throws IOException {
        valueNext();
        outputContext.write(INTEGER_PREFIX);
        outputContext.write(v);
        outputContext.write(END_SUFFIX);
    }

    @Override
    public void writeNumber(long v) throws IOException {
        valueNext();
        outputContext.write(INTEGER_PREFIX);
        outputContext.write(v);
        outputContext.write(END_SUFFIX);
    }

    @Override
    public void writeNumber(BigInteger v) throws IOException {
        valueNext();
        outputContext.write(INTEGER_PREFIX);
        outputContext.write(v.toString());
        outputContext.write(END_SUFFIX);
    }

    private void valueNext() throws JsonGenerationException {
        try {
            ctx.valueNext();
        } catch (IOException e) {
            throw new JsonGenerationException(e.getMessage());
        }
    }

    @Override
    public void writeNumber(double d) throws IOException {
        throw new UnsupportedOperationException("floating point types are not supported by BEncode specification");
    }

    @Override
    public void writeNumber(float f) throws IOException {
        throw new UnsupportedOperationException("floating point types are not supported by BEncode specification");
    }

    @Override
    public void writeNumber(BigDecimal dec) throws IOException {
        throw new UnsupportedOperationException("floating point types are not supported by BEncode specification");
    }

    @Override
    public void writeNumber(String encodedValue) throws IOException {
        writeString(encodedValue);
    }

    @Override
    public void writeBoolean(boolean state) throws IOException {
        valueNext();
        outputContext.write(state ? TRUE_VALUE : FALSE_VALUE);
    }

    @Override
    public void writeNull() throws IOException {
        valueNext();
        outputContext.write(NULL_VALUE);
    }
}
