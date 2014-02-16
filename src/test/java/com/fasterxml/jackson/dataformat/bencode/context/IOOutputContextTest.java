package com.fasterxml.jackson.dataformat.bencode.context;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class IOOutputContextTest {
    @Test
    public void testWriteInts() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        IOOutputContext o = new IOOutputContext(bos, Charset.forName("UTF-8"));

        o.write(BigInteger.valueOf(Long.MAX_VALUE).shiftLeft(1));
        assertThat(bos.toByteArray(), is("18446744073709551614".getBytes("ISO-8859-1")));

        final int reps = 500;
        final Charset asis = Charset.forName("ASCII");
        BigInteger toBeEncoded = new BigInteger("34028236692079938463463374607431768211455");
//        final BigInteger toBeEncoded = new BigInteger("340282366920938463463374607431768211455");
        long time, longLikeTime, stringieTime;
        final int hash = Arrays.hashCode(IOOutputContext.getByteBuf(toBeEncoded));

        time = System.currentTimeMillis();
        for (int i = 0; i < reps; i++) {
            assert Arrays.hashCode(toBeEncoded.toString().getBytes(asis)) == hash;
        }
        stringieTime = System.currentTimeMillis() - time;

        time = System.currentTimeMillis();
        for (int i = 0; i < reps; i++) {
            assert Arrays.hashCode(IOOutputContext.getByteBuf(toBeEncoded)) == hash;
        }
        longLikeTime = System.currentTimeMillis() - time;

        System.out.println(String.format("longlike: %d", longLikeTime));
        System.out.println(String.format("stringie: %d", stringieTime));
    }
}
