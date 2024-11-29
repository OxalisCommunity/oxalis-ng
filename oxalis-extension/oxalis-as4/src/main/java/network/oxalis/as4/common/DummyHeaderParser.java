package network.oxalis.as4.common;

import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.api.header.HeaderParser;
import network.oxalis.api.util.Type;
import network.oxalis.vefa.peppol.common.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

@Slf4j
@Singleton
@Type("dummy")
public class DummyHeaderParser implements HeaderParser {

    @Override
    public Header parse(InputStream inputStream) {

        log.debug("DummyHeaderParser: parse");

        try {
            byte[] drain = new byte[500];
            inputStream.read(drain);
        } catch (IOException e) {
            log.error("IOException while parsing header", e);
        }

        return Header.of(
                ParticipantIdentifier.of("DummySender"),
                ParticipantIdentifier.of("DummyReceiver"),
                ProcessIdentifier.of("DummyProcess"),
                DocumentTypeIdentifier.of("DummyDocument"),
                C1CountryIdentifier.of("DummyCountry"),
                InstanceIdentifier.of("DummyInstance"),
                InstanceType.of("Dummy", "InstanceType", "1.0"),
                new Date(0L));
    }
}
