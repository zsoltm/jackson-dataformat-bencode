package com.fasterxml.jackson.dataformat.bencode.context;

import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

public class BContextDictionary extends BContext {
    private String prevKey;

    public BContextDictionary(BContext parent) {
        this.parent = parent;
        _type = TYPE_OBJECT;
        expected = Expect.KEY;
    }

    @Override
    public Expect valueNext() throws IOException {
        if (expected != Expect.VALUE) throw new IOException("unexpected value");
        parent.incIndex();
        expected = Expect.KEY;
        return Expect.VALUE;
    }

    @Override
    public Expect keyNext(String key) throws IOException {
        if (expected != Expect.KEY) throw new IOException("unexpected key");
        if (prevKey != null) {
            int compareResult = prevKey.compareTo(key);
            if (compareResult >= 0) {
                throw new IOException(compareResult == 0 ?
                        "duplicate dictionary key" : "keys must be in lexicographically ascending order");
            }
        }
        prevKey = key;
        expected = Expect.VALUE;
        return Expect.KEY;
    }

    @Override
    public BContext createChildDictionary() {
        return new BContextDictionary(this);
    }

    @Override
    public BContext changeToParent() throws IOException {
        if (expected != Expect.KEY) throw new IOException("uneven dictionary contents");
        return super.changeToParent();
    }

    @Override
    public String getCurrentName() {
        return prevKey;
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
