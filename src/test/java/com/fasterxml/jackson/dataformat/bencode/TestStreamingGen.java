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
            assertThat(e.getMessage(), is("keys must be in lexicographically ascending order"));
        }
        try {
            underTest.writeFieldName("amet");
            fail("should throw exception");
        } catch (JsonProcessingException e) {
            assertThat(e.getMessage(), is("keys must be in lexicographically ascending order"));
        }
        underTest.writeFieldName("phasellus");
        underTest.writeString("tincidunt");

        underTest.writeFieldName("vitae");

        underTest.writeStartObject();
        underTest.writeNumberField("eget", 0);

        try {
            underTest.writeFieldName("eget");
            fail("shouldn't allow duplicate dictionary keys");
        } catch (JsonProcessingException e) {
            assertThat(e.getMessage(), is("duplicate dictionary key"));
        }

        underTest.writeEndObject();

        underTest.writeEndObject();

        assertThat(out.toString("ISO-8859-1"), is("d5:lorem5:ipsum9:phasellus9:tincidunt5:vitaed4:egeti0eee"));
    }

    @Test
    public void testContextAwareness() throws Exception {
        try {
            underTest.writeFieldName("bsd");
            fail("shouldn't allow field names in root context");
        } catch (JsonProcessingException e) {
            assertThat(e.getMessage(), is("not in dictionary"));
        }

        underTest.writeStartObject();
        try {
            underTest.writeNumber(65534);
            fail("shouldn't allow values in place of keys");
        } catch (JsonProcessingException e) {
            assertThat(e.getMessage(), is("unexpected value"));
        }

        underTest.writeFieldName("asd");

        try {
            underTest.writeFieldName("bsd");
            fail("shouldn't allow keys right after keys");
        } catch (JsonProcessingException e) {
            assertThat(e.getMessage(), is("unexpected key"));
        }

        try {
            underTest.writeEndObject();
            fail("shouldn't allow closing the dictionary after a key");
        } catch (JsonProcessingException e) {
            assertThat(e.getMessage(), is("uneven dictionary contents"));
        }

        underTest.writeStartArray();
        underTest.writeString("hello");
        underTest.writeNumber(3);

        try {
            underTest.writeFieldName("fxs");
            fail("shouldn't allow keys in arrays");
        } catch (JsonProcessingException e) {
            assertThat(e.getMessage(), is("not in dictionary"));
        }

        underTest.writeEndArray();

        underTest.writeStringField("field", "after array");

        underTest.writeEndObject();

        assertThat(out.toString("ISO-8859-1"), is("d3:asdl5:helloi3ee5:field11:after arraye"));
    }

    @Test
    public void testTypes() throws Exception {
        underTest.writeStartArray();
        underTest.writeString("árvíztűrő tükörfúrógép");
        underTest.writeNull();
        underTest.writeString("skip text".toCharArray(), 5, 4);
        underTest.writeEndArray();

        assertThat(out.toString("ISO-8859-1"), is(
                "l31:" + new String("árvíztűrő tükörfúrógép".getBytes("UTF-8"), "ISO-8859-1") +
                        "4:null" +
                        "4:texte"));

    }

    @Test
    public void testRaw() throws Exception {
        underTest.writeRaw("árvíztűrő tükörfúrógép".toCharArray(), 0, 22);
        underTest.writeRaw("árvíztűrő tükörfúrógép");
        underTest.writeRaw("árvíztűrő tükörfúrógép", 10, 12);
        underTest.writeRaw('ű');
        assertThat(out.toByteArray(),
                is("árvíztűrő tükörfúrógépárvíztűrő tükörfúrógéptükörfúrógépű".getBytes("UTF-8")));
    }
}
