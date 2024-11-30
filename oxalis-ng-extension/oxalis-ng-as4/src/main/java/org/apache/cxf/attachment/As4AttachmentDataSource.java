package org.apache.cxf.attachment;

import lombok.Getter;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Getter
public class As4AttachmentDataSource extends AttachmentDataSource {

    List<InputStream> inputStreams = new ArrayList<>();

    public As4AttachmentDataSource(String ctParam, InputStream inParam) throws IOException {
        super(ctParam, inParam);
    }

    @Override
    public InputStream getInputStream() {
        InputStream inputStream = super.getInputStream();
        inputStreams.add(inputStream);
        return inputStream;
    }

    public void closeAll() {
        inputStreams.forEach(this::close);
    }

    @SneakyThrows
    private void close(InputStream inputStream) {
        inputStream.close();
    }
}
