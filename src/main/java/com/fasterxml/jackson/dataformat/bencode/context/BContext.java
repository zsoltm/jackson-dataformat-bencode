package com.fasterxml.jackson.dataformat.bencode.context;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;

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

    public Expect valueNext() throws JsonProcessingException {
        return expected;
    }

    public Expect keyNext(String key) throws JsonProcessingException {
        throw new JsonGenerationException("not in dictionary");
    }

    public BContext changeToParent() throws JsonProcessingException {
        if (parent == null) {
            throw new JsonGenerationException("trying to access parent of root");
        }
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
