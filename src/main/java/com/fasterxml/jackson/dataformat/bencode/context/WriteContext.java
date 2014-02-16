package com.fasterxml.jackson.dataformat.bencode.context;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;

public class WriteContext {
    enum Type {
        ROOT, LIST, DICT
    }

    enum Expect {
        KEY, VALUE
    }

    final WriteContext parent;
    final Type type;

    protected Expect expect;

    WriteContext(WriteContext parent, Type type) {
        this.parent = parent;
        this.type = type;
    }

    public WriteContext() {
        this(null, Type.ROOT);
    }

    public WriteContext createChildDictionary() {
        return new WriteContextDictionary(this, Type.DICT);
    }

    public WriteContext createChildList() {
        return new WriteContext(this, Type.LIST);
    }

    public final boolean inList() {
        return type == Type.LIST;
    }

    public final boolean inDict() {
        return type == Type.DICT;
    }

    public Expect writeValue() throws JsonProcessingException {
        return Expect.VALUE;
    }

    public Expect writeKey(String key) throws JsonProcessingException {
        throw new JsonGenerationException("not in dictionary");
    }

    public WriteContext changeToParent() throws JsonProcessingException {
        if (parent == null) {
            throw new JsonGenerationException("trying to access parent of root");
        }
        return parent;
    }
}
