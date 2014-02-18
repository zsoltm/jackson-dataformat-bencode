package com.fasterxml.jackson.dataformat.bencode.context;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;

public class BContext {
    enum Type {
        ROOT, LIST, DICT
    }

    public enum Expect {
        KEY, VALUE
    }

    final BContext parent;
    final Type type;

    protected Expect expected = Expect.VALUE;

    BContext(BContext parent, Type type) {
        this.parent = parent;
        this.type = type;
    }

    public BContext() {
        this(null, Type.ROOT);
    }

    public BContext createChildDictionary() {
        return new BContextDictionary(this, Type.DICT);
    }

    public BContext createChildList() {
        return new BContextList(this, Type.LIST);
    }

    public final boolean inList() {
        return type == Type.LIST;
    }

    public final boolean inDict() {
        return type == Type.DICT;
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
