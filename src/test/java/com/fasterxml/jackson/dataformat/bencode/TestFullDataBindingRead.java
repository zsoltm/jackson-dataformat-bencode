package com.fasterxml.jackson.dataformat.bencode;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.bencode.types.User;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TestFullDataBindingRead {
    private ObjectMapper underTest;

    @Before
    public void startUp() throws IOException {
        underTest = new BEncodeMapper();
    }

    @Test
    public void testReadValueFromStream() throws Exception {
        InputStream in = new ByteArrayInputStream(TestUtils.TUTORIAL_EXAMPLE_ENCODED.getBytes("ISO-8859-1"));
        User u = underTest.readValue(in, User.class);
    }
}
