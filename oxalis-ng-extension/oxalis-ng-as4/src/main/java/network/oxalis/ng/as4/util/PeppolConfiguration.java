package network.oxalis.ng.as4.util;

import lombok.Getter;
import network.oxalis.ng.api.tag.Tag;

import java.util.Arrays;
import java.util.List;

import static org.apache.wss4j.dom.handler.WSHandlerConstants.ENCRYPT;
import static org.apache.wss4j.dom.handler.WSHandlerConstants.SIGNATURE;

@Getter
public class PeppolConfiguration implements Tag {
    public static final String IDENTIFIER = "AS4.OUTBOUND.PEPPOL";

    private List<String> actions = Arrays.asList(SIGNATURE, ENCRYPT);

    private String partyIDType = "urn:fdc:peppol.eu:2017:identifiers:ap";
    private String agreementRef = "urn:fdc:peppol.eu:2017:agreements:tia:ap_provider";

    private String fromRole = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator";
    private String toRole = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder";


    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }
}
