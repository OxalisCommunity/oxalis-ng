package network.oxalis.ng.as4.inbound;

import lombok.Data;
import network.oxalis.ng.as4.common.As4MessageProperties;

import java.util.ArrayList;
import java.util.List;

@Data
public class As4EnvelopeHeader {

    private String messageId;
    private String conversationId;

    private List<String> fromPartyId;
    private String fromPartyRole;

    private List<String> toPartyId;
    private String toPartyRole;

    private String service;
    private String action;

    private As4MessageProperties messageProperties = new As4MessageProperties();

    private List<String> payloadCIDs = new ArrayList<>();
}
