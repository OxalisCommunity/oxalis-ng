package network.oxalis.ng.as4.inbound;

import lombok.Getter;
import lombok.ToString;
import network.oxalis.vefa.peppol.common.model.*;

import jakarta.xml.soap.MimeHeader;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Getter
@ToString
public class As4PayloadHeader extends Header {

    private final Header header;
    private final Collection<MimeHeader> mimeHeaders;
    private final String cid;

    private final String conversationId;

    public As4PayloadHeader(Header header, Collection<MimeHeader> mimeHeaders, String cid, String conversationId) {
        this.header = header;
        this.mimeHeaders = mimeHeaders;
        this.cid = cid;
        this.conversationId = conversationId;
    }

    @Override
    public Header sender(ParticipantIdentifier sender) {
        return header.sender(sender);
    }

    @Override
    public Header receiver(ParticipantIdentifier receiver) {
        return header.receiver(receiver);
    }

    @Override
    public Header process(ProcessIdentifier process) {
        return header.process(process);
    }

    @Override
    public Header documentType(DocumentTypeIdentifier documentType) {
        return header.documentType(documentType);
    }

    @Override
    public Header identifier(InstanceIdentifier identifier) {
        return header.identifier(identifier);
    }

    @Override
    public Header instanceType(InstanceType instanceType) {
        return header.instanceType(instanceType);
    }

    @Override
    public Header creationTimestamp(Date creationTimestamp) {
        return header.creationTimestamp(creationTimestamp);
    }

    @Override
    public Header c1CountryIdentifier(C1CountryIdentifier c1CountryIdentifier) {
        return header.c1CountryIdentifier(c1CountryIdentifier);
    }

    @Override
    public Header argument(ArgumentIdentifier identifier) {
        return header.argument(identifier);
    }

    @Override
    public Header arguments(List<ArgumentIdentifier> extras) {
        return header.arguments(extras);
    }

    @Override
    public ArgumentIdentifier getArgument(String key) {
        return header.getArgument(key);
    }

    @Override
    public List<ArgumentIdentifier> getArguments() {
        return header.getArguments();
    }

    @Override
    public boolean equals(Object o) {
        return header.equals(o);
    }

    @Override
    public int hashCode() {
        return header.hashCode();
    }

    @Override
    public ParticipantIdentifier getSender() {
        return header.getSender();
    }

    @Override
    public ParticipantIdentifier getReceiver() {
        return header.getReceiver();
    }

    @Override
    public ProcessIdentifier getProcess() {
        return header.getProcess();
    }

    @Override
    public DocumentTypeIdentifier getDocumentType() {
        return header.getDocumentType();
    }

    @Override
    public InstanceIdentifier getIdentifier() {
        return header.getIdentifier();
    }

    @Override
    public InstanceType getInstanceType() {
        return header.getInstanceType();
    }

    @Override
    public Date getCreationTimestamp() {
        return header.getCreationTimestamp();
    }

    @Override
    public C1CountryIdentifier getC1CountryIdentifier() {
        return header.getC1CountryIdentifier();
    }
}
