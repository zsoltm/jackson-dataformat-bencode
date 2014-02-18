package com.fasterxml.jackson.dataformat.bencode.context;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;

public class BContextDictionary extends BContext {
    private String prevKey;

    public BContextDictionary(BContext parent, Type type) {
        super(parent, type);
        expected = Expect.KEY;
    }

    @Override
    public Expect valueNext() throws JsonProcessingException {
        if (expected != Expect.VALUE) throw new JsonGenerationException("unexpected value");
        expected = Expect.KEY;
        return Expect.VALUE;
    }

    @Override
    public Expect keyNext(String key) throws JsonProcessingException {
        if (expected != Expect.KEY) throw new JsonGenerationException("unexpected key");
        if (prevKey != null) {
            int compareResult = prevKey.compareTo(key);
            if (compareResult >= 0) {
                throw new JsonGenerationException(compareResult == 0 ?
                        "duplicate dictionary key" : "keys must be in lexicographically ascending order");
            }
        }
        prevKey = key;
        expected = Expect.VALUE;
        return Expect.KEY;
    }

    @Override
    public BContext changeToParent() throws JsonProcessingException {
        if (expected != Expect.KEY) throw new JsonGenerationException("uneven dictionary contents");
        return super.changeToParent();
    }

    @Override
    public JsonToken getStartToken() {
        return JsonToken.START_OBJECT;
    }

    @Override
    public JsonToken getEndToken() {
        return JsonToken.END_OBJECT;
    }
}
