package com.fasterxml.jackson.dataformat.bencode.context;

import com.fasterxml.jackson.core.JsonToken;

public class BContextList extends BContext {

    public BContextList(BContext parent, Type type) {
        super(parent, type);
    }

    @Override
    public JsonToken getStartToken() {
        return JsonToken.START_ARRAY;
    }

    @Override
    public JsonToken getEndToken() {
        return JsonToken.END_ARRAY;
    }
}
