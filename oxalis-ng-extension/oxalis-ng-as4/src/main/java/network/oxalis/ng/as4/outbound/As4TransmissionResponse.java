package network.oxalis.ng.as4.outbound;

import network.oxalis.ng.api.model.TransmissionIdentifier;
import network.oxalis.ng.api.outbound.TransmissionRequest;
import network.oxalis.ng.api.outbound.TransmissionResponse;
import network.oxalis.ng.api.timestamp.Timestamp;
import network.oxalis.ng.as4.lang.OxalisAs4TransmissionException;
import network.oxalis.vefa.peppol.common.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class As4TransmissionResponse implements TransmissionResponse {

    // Original transmission request is kept to allow easy access to immutable objects part of the request.
    private final TransmissionRequest transmissionRequest;
    private final TransmissionIdentifier transmissionIdentifier;
    private final Digest digest;
    private final Receipt receipt;
    private final List<Receipt> receipts;
    private final Date timestamp;
    private final OxalisAs4TransmissionException transmissionException;

    public As4TransmissionResponse(TransmissionIdentifier transmissionIdentifier,
                                   TransmissionRequest transmissionRequest, Digest digest,
                                   byte[] nativeEvidenceBytes, Timestamp timestamp, Date date) {
        this.transmissionIdentifier = transmissionIdentifier;
        this.transmissionRequest = transmissionRequest;
        this.digest = digest;
        this.receipt = Receipt.of("message/disposition-notification", nativeEvidenceBytes);
        this.timestamp = date;
        this.transmissionException = null;

        List<Receipt> receiptList = new ArrayList<>();
        receiptList.add(receipt);
        timestamp.getReceipt().ifPresent(receiptList::add);

        this.receipts = Collections.unmodifiableList(receiptList);
    }

    public As4TransmissionResponse(TransmissionIdentifier transmissionIdentifier, TransmissionRequest transmissionRequest, OxalisAs4TransmissionException transmissionException) {
        this.transmissionRequest = transmissionRequest;
        this.transmissionIdentifier = transmissionIdentifier;
        this.transmissionException = transmissionException;
        this.digest = null;
        this.receipt = null;
        this.receipts = null;
        this.timestamp = null;
    }

    @Override
    public Header getHeader() {
        return transmissionRequest.getHeader();
    }

    public TransmissionIdentifier getTransmissionIdentifier() {
        return transmissionIdentifier;
    }

    @Override
    public List<Receipt> getReceipts() {
        return receipts;
    }

    @Override
    public Endpoint getEndpoint() {
        return transmissionRequest.getEndpoint();
    }

    @Override
    public Receipt primaryReceipt() {
        return receipt;
    }

    @Override
    public Digest getDigest() {
        return digest;
    }

    @Override
    public TransportProtocol getTransportProtocol() {
        return TransportProtocol.AS4;
    }

    @Override
    public Date getTimestamp() {
        return timestamp;
    }

    public OxalisAs4TransmissionException getTransmissionException() {
        return transmissionException;
    }
}
