package network.oxalis.ng.as4.outbound;

import com.google.inject.Inject;
import network.oxalis.ng.api.lang.OxalisTransmissionException;
import network.oxalis.ng.api.outbound.MessageSender;
import network.oxalis.ng.api.outbound.TransmissionRequest;
import network.oxalis.ng.api.outbound.TransmissionResponse;

public class As4MessageSenderFacade implements MessageSender {

    private As4MessageSender messageSender;

    @Inject
    public As4MessageSenderFacade(As4MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    @Override
    public TransmissionResponse send(TransmissionRequest transmissionRequest) throws OxalisTransmissionException {
        return messageSender.send(transmissionRequest);
    }
}
