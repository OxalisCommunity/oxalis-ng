package network.oxalis.ng.as4.inbound;

import network.oxalis.ng.api.inbound.InboundMetadata;
import network.oxalis.ng.api.model.TransmissionIdentifier;
import network.oxalis.ng.api.tag.Tag;
import network.oxalis.ng.api.timestamp.Timestamp;
import network.oxalis.vefa.peppol.common.model.*;

import java.security.cert.X509Certificate;
import java.util.*;

public class As4InboundMetadata implements InboundMetadata {

    private final TransmissionIdentifier transmissionIdentifier;
    private final String conversationId;
    private final Header header;
    private final Date timestamp;
    private final TransportProfile transportProfile;
    private final Digest digest;
    private final Receipt primaryReceipt;
    private final List<Receipt> receipts;
    private final X509Certificate certificate;
    private final As4EnvelopeHeader as4EnvelopeHeader;


    public As4InboundMetadata(TransmissionIdentifier transmissionIdentifier, String conversationId, Header header, Timestamp timestamp,
                              TransportProfile transportProfile, Digest digest, X509Certificate certificate,
                              byte[] primaryReceipt, As4EnvelopeHeader as4EnvelopeHeader) {
        this.transmissionIdentifier = transmissionIdentifier;
        this.conversationId = conversationId;
        this.header = header;
        this.timestamp = timestamp.getDate();
        this.transportProfile = transportProfile;
        this.digest = digest;
        this.certificate = certificate;
        this.primaryReceipt = Receipt.of("message/disposition-notification", primaryReceipt);

        List<Receipt> receipts = new ArrayList<>();
        receipts.add(this.primaryReceipt);
        if (timestamp.getReceipt().isPresent())
            receipts.add(timestamp.getReceipt().get());
        this.receipts = Collections.unmodifiableList(receipts);

        this.as4EnvelopeHeader = as4EnvelopeHeader;
    }

    @Override
    public X509Certificate getCertificate() {
        return certificate;
    }

    @Override
    public TransmissionIdentifier getTransmissionIdentifier() {
        return transmissionIdentifier;
    }

    public String getConversationId() {
        return conversationId;
    }

    @Override
    public Header getHeader() {
        return header;
    }

    @Override
    public Date getTimestamp() {
        return timestamp;
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
    public TransportProfile getProtocol() {
        return transportProfile;
    }

    @Override
    public List<Receipt> getReceipts() {
        return receipts;
    }

    @Override
    public Receipt primaryReceipt() {
        return primaryReceipt;
    }

    @Override
    public Tag getTag() {
        return Tag.NONE;
    }

    public As4EnvelopeHeader getAs4EnvelopeHeader() {
        return as4EnvelopeHeader;
    }
}
