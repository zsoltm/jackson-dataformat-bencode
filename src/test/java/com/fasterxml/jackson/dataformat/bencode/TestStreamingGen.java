package com.fasterxml.jackson.dataformat.bencode;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class TestStreamingGen {
    protected BEncodeFactory bEncodeFactory = new BEncodeFactory();
    protected BEncodeGenerator underTest; // generator under test
    protected ByteArrayOutputStream out;

    public enum Gender { MALE, FEMALE }

    @Before
    public void startUp() throws IOException {
        out = new ByteArrayOutputStream();
        underTest = bEncodeFactory.createGenerator(out);
    }


    @Test
    public void testSimpleEntities() throws Exception {
    }

    @Test
    public void tutorialTest() throws Exception {
//        JsonGenerator underTest = new JsonFactory().createGenerator(out);
        byte [] binaryData = DatatypeConverter.parseHexBinary("E3811B9539CACFF680E418124272177C47477157");

        underTest.writeStartObject();
        underTest.writeStringField("gender", Gender.MALE.name());
        underTest.writeObjectFieldStart("name");
        underTest.writeStringField("first", "Joe");
        underTest.writeStringField("last", "Sixpack");
        underTest.writeEndObject();
        underTest.writeBinaryField("userImage", binaryData);
        underTest.writeBooleanField("verified", false);
        underTest.writeEndObject();
        underTest.close();

        assertThat(out.toString("ISO-8859-1"), is("d6:gender4:MALE4:named5:first3:Joe4:last7:Sixpacke9:userImage20:" +
                new String(binaryData, "ISO-8859-1") + "8:verified5:falsee"));
    }

    @Test
    public void nonLexicographicalKeyOrderingShouldThrow() throws Exception {
        underTest.writeStartObject();
        underTest.writeStringField("lorem", "ipsum");
        try {
            underTest.writeStringField("dolor", "sit");
            fail("should throw exception");
        } catch (JsonProcessingException e) {
            //
        }
        try {
            underTest.writeFieldName("amet");
            fail("should throw exception");
        } catch (JsonProcessingException e) {
            //
        }
        underTest.writeFieldName("phasellus");
        underTest.writeString("tincidunt");

        underTest.writeFieldName("vitae");
        underTest.writeStartObject();
        underTest.writeNumberField("eget", 0);
//        underTest.writeNumberField("tempus", 0);

        underTest.writeEndObject();

        underTest.writeEndObject();

        assertThat(out.toString("ISO-8859-1"), is("d5:lorem5:ipsum9:phasellus9:tincidunt5:vitaed4:egeti0eee"));
    }
}
