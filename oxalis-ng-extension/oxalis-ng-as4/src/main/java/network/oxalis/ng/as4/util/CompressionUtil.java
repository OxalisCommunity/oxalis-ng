package network.oxalis.ng.as4.util;

import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.io.CacheSizeExceededException;
import org.apache.cxf.io.CachedOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPOutputStream;

public class CompressionUtil {

    /**
     * Gets Compressed Stream for given input Stream
     *
     * @param sourceStream : Input Stream to be compressed to
     * @return Compressed Stream
     * @throws IOException when some thing bad happens
     */
    public InputStream getCompressedStream(final InputStream sourceStream) throws IOException {

        if (sourceStream == null) {
            throw new IllegalArgumentException("Source Stream cannot be NULL");
        }

        CachedOutputStream cache = new CachedOutputStream();

        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(cache)) {
            IOUtils.copyAndCloseInput(sourceStream, gzipOutputStream);
            gzipOutputStream.flush();
            gzipOutputStream.finish();
            return cache.getInputStream();
        } catch (CacheSizeExceededException | IOException cee) {
            sourceStream.close();
            throw cee;
        }
    }
}