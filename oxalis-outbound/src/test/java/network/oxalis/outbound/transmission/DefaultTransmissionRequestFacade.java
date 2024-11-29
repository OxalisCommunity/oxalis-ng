package network.oxalis.outbound.transmission;

import network.oxalis.api.outbound.TransmissionMessage;
import network.oxalis.api.outbound.TransmissionRequest;
import network.oxalis.api.tag.Tag;
import network.oxalis.vefa.peppol.common.model.Endpoint;
import network.oxalis.vefa.peppol.common.model.Header;

import java.io.InputStream;
import java.io.Serializable;

public class DefaultTransmissionRequestFacade implements TransmissionRequest, Serializable {

    private static final long serialVersionUID = -4542158937465140099L;

    private DefaultTransmissionRequest defaultTransmissionRequest;

    public DefaultTransmissionRequestFacade(TransmissionMessage transmissionMessage, Endpoint endpoint) {
        defaultTransmissionRequest = new DefaultTransmissionRequest(transmissionMessage, endpoint);
    }

    @Override
    public Endpoint getEndpoint() {
        return defaultTransmissionRequest.getEndpoint();
    }

    @Override
    public Header getHeader() {
        return defaultTransmissionRequest.getHeader();
    }

    @Override
    public InputStream getPayload() {
        return defaultTransmissionRequest.getPayload();
    }

    @Override
    public Tag getTag() {
        return defaultTransmissionRequest.getTag();
    }
}
