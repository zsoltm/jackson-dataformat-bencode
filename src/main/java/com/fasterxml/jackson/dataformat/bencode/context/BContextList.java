package com.fasterxml.jackson.dataformat.bencode.context;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;

public class BContextList extends BContext {

    public BContextList(BContext parent) {
        this.parent = parent;
        _type = TYPE_ARRAY;
        _index = 0;
    }

    @Override
    public Expect valueNext() throws JsonProcessingException {
        _index++;
        return super.valueNext();
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
