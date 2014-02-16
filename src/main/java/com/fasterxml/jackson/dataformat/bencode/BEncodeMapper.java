package com.fasterxml.jackson.dataformat.bencode;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class BEncodeMapper extends ObjectMapper {

    public BEncodeMapper() {
        super(new BEncodeFactory());
        enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
        enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
}
