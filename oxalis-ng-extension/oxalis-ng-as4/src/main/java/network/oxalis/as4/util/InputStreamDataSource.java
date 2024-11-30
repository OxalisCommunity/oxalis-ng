package network.oxalis.as4.util;

import jakarta.activation.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class InputStreamDataSource implements DataSource {

    private final InputStream inputStream;

    private final String contentType;

    public InputStreamDataSource(InputStream inputStream, String contentType) {
        this.inputStream = inputStream;
        this.contentType = contentType;
    }

    @Override
    public InputStream getInputStream () throws IOException {
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream () {
        throw new UnsupportedOperationException("Read-only");
    }

    @Override
    public String getContentType () {
        return contentType;
    }

    @Override
    public String getName () {
        throw new UnsupportedOperationException("DataSource name not available");
    }
}
