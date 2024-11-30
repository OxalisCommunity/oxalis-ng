package network.oxalis.as4.util;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Random;
import java.util.zip.GZIPInputStream;

public class CompressionUtilTest {
    @Test
    public void simple() throws Exception {
        byte[] before = "Lorem ipsum dolor sit amet".getBytes();
        InputStream sourceStream = new ByteArrayInputStream(before);
        InputStream compressedStream = new CompressionUtil().getCompressedStream(sourceStream);
        try (GZIPInputStream decompressedStream = new GZIPInputStream(compressedStream)) {
            byte[] after = IOUtils.toByteArray(decompressedStream);
            Assert.assertEquals(before, after);
        }
    }

    @Test
    public void cachedInTempFile() throws Exception {
        byte[] before = new byte[1024 * 1024];
        new Random().nextBytes(before);
        InputStream sourceStream = new ByteArrayInputStream(before);
        InputStream compressedStream = new CompressionUtil().getCompressedStream(sourceStream);
        try (GZIPInputStream decompressedStream = new GZIPInputStream(compressedStream)) {
            byte[] after = IOUtils.toByteArray(decompressedStream);
            Assert.assertEquals(before, after);
        }
    }
}
