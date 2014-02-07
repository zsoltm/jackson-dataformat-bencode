package com.fasterxml.jackson.dataformat.bencode.context;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;

public class WriteContextDictionary extends WriteContext {

    private String prevKey;

    public WriteContextDictionary(WriteContext parent, Type type) {
        super(parent, type);
        expect = Expect.KEY;
    }

    @Override
    public Expect writeValue() throws JsonProcessingException {
        if (expect != Expect.VALUE) throw new JsonGenerationException("unexpected value");
        expect = Expect.KEY;
        return Expect.VALUE;
    }

    @Override
    public Expect writeKey(String key) throws JsonProcessingException {
        if (expect != Expect.KEY) throw new JsonGenerationException("unexpected key");
        if (prevKey != null) {
            int compareResult = prevKey.compareTo(key);
            if (compareResult >= 0) {
                throw new JsonGenerationException(compareResult == 0 ?
                        "duplicate dictionary key" : "keys must be in lexicographically ascending order");
            }
        }
        prevKey = key;
        expect = Expect.VALUE;
        return Expect.KEY;
    }

    @Override
    public WriteContext changeToParent() throws JsonProcessingException {
        if (expect != Expect.KEY) throw new JsonGenerationException("uneven dictionary contents");
        return super.changeToParent();
    }
}
