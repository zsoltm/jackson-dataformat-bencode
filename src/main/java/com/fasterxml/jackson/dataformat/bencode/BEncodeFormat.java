package com.fasterxml.jackson.dataformat.bencode;

import java.nio.charset.Charset;

public class BEncodeFormat {
    static final byte INTEGER_PREFIX = 'i';
    static final byte LIST_PREFIX = 'l';
    static final byte DICTIONARY_PREFIX = 'd';
    static final byte END_SUFFIX = 'e';
    static final byte STRING_SEPARATOR = ':';

    static final Charset UTF_8 = Charset.forName("UTF-8");
}
