package network.oxalis.ng.as4.inbound;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AS4MessageContextKey {

    public static final String FIRST_PAYLOAD_PATH = "network.oxalis.as4.first.payload.path";
    public static final String FIRST_PAYLOAD_HEADER = "network.oxalis.as4.first.payload.header";
    public static final String ENVELOPE_HEADER = "network.oxalis.as4.envelope.header";
    public static final String PERSISTED = "network.oxalis.as4.persisted";
}
