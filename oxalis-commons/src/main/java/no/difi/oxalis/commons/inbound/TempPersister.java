package no.difi.oxalis.commons.inbound;

import com.google.common.io.ByteStreams;
import no.difi.oxalis.api.inbound.ContentPersister;
import no.difi.oxalis.api.inbound.InboundMetadata;
import no.difi.oxalis.api.inbound.ReceiptPersister;
import no.difi.vefa.peppol.common.model.Header;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class TempPersister implements ContentPersister, ReceiptPersister {

    @Override
    public Path persist(Header header, InputStream inputStream) throws IOException {
        // Create temp file
        Path path = Files.createTempFile("oxalis-inbound", ".message.dat");

        // Copy content to temp file
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            ByteStreams.copy(inputStream, outputStream);
        }

        // Return file name
        return path;
    }

    @Override
    public Path persist(InboundMetadata inboundMetadata) throws IOException {
        // Create temp file
        Path path = Files.createTempFile("oxalis-inbound", ".mdn.dat");

        // Copy content to temp file
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            // TODO ByteStreams.copy(inputStream, outputStream);
        }

        // Return file name
        return path;
    }
}
