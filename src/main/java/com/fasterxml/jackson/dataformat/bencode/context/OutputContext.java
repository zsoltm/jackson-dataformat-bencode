package com.fasterxml.jackson.dataformat.bencode.context;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.math.BigInteger;
import java.nio.charset.Charset;

public interface OutputContext {
    Charset getCharset();

    OutputStream getOutputStream() throws IOException;

    Writer getWriter() throws IOException;

    void write(String text) throws IOException;

    void write(byte b) throws IOException;

    void write(int i) throws IOException;

    void write(long i) throws IOException;

    void write(byte[] data, int offset, int len) throws IOException;

    void write(byte[] bytes) throws IOException;

    void write(char[] data, int offset, int len) throws IOException;

    void write(char[] chars) throws IOException;

    void write(BigInteger i) throws IOException;

    void close() throws IOException;

    void flush() throws IOException;
}
