package com.fasterxml.jackson.dataformat.bencode;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class TestUtils {
    static byte [] BINARY_DATA = DatatypeConverter.parseHexBinary("E3811B9539CACFF680E418124272177C47477157");
    public static final String TUTORIAL_EXAMPLE_ENCODED =
            "d6:gender4:MALE4:named5:first3:Joe4:last7:Sixpacke9:userImage20:" +
                    new String(BINARY_DATA, Charset.forName("ISO-8859-1")) + "8:verified5:falsee";

    public static byte[] readFileBinary(String path) {
        InputStream is = TestUtils.class.getResourceAsStream(path);
        int nRead;
        byte[] data = new byte[1 << 12];
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return buffer.toByteArray();

    }
}
