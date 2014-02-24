package com.fasterxml.jackson.dataformat.bencode.context;

import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

public class BContext extends JsonStreamContext {
    void incIndex() {
        _index++;
    }

//    enum Type {
//        ROOT, LIST, DICT
//    }

    public enum Expect {
        KEY, VALUE
    }

    protected BContext parent;
    protected Expect expected = Expect.VALUE;

    @Override
    public JsonStreamContext getParent() {
        return parent;
    }

    @Override
    public String getCurrentName() {
        return null;
    }

    BContext(BContext parent) {
        this.parent = parent;
        _type = TYPE_ROOT;
    }

    public BContext() {
        this(null);
    }

    public BContext createChildDictionary() {
        return new BContextDictionary(this);
    }

    public BContext createChildList() {
        return new BContextList(this);
    }

    public Expect valueNext() throws IOException {
        return expected;
    }

    public Expect keyNext(String key) throws IOException {
        throw new IOException("not in dictionary");
    }

    public BContext changeToParent() throws IOException {
        if (parent == null) throw new IOException("trying to access parent of root");
        return parent;
    }

    public JsonToken getStartToken() {
        return null;
    }

    public JsonToken getEndToken() {
        return null;
    }

    public Expect getExpected() {
        return expected;
    }
}
