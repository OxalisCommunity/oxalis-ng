package network.oxalis.ng.as4.outbound;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import network.oxalis.ng.api.outbound.TransmissionRequest;
import network.oxalis.ng.as4.api.MessageIdGenerator;
import network.oxalis.ng.as4.common.As4MessageProperties;
import network.oxalis.ng.as4.util.PeppolConfiguration;
import network.oxalis.ng.as4.util.TransmissionRequestUtil;
import network.oxalis.ng.commons.security.CertificateUtils;
import network.oxalis.vefa.peppol.common.model.ProcessIdentifier;
import org.apache.cxf.attachment.AttachmentUtil;
import org.apache.cxf.message.Attachment;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.*;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Spliterator;

import static network.oxalis.ng.as4.util.Constants.TEST_ACTION;
import static network.oxalis.ng.as4.util.Constants.TEST_SERVICE;
import static network.oxalis.ng.as4.util.GeneralUtils.iteratorToStreamOfUnknownSize;

public class MessagingProvider {

    private final X509Certificate certificate;
    private final MessageIdGenerator messageIdGenerator;
    private final PeppolConfiguration defaultOutboundConfiguration;
    private final ActionProvider actionProvider;

    @Inject
    public MessagingProvider(X509Certificate certificate, MessageIdGenerator messageIdGenerator, PeppolConfiguration defaultOutboundConfiguration, ActionProvider actionProvider) {
        this.certificate = certificate;
        this.messageIdGenerator = messageIdGenerator;
        this.defaultOutboundConfiguration = defaultOutboundConfiguration;
        this.actionProvider = actionProvider;
    }

    public Messaging createMessagingHeader(TransmissionRequest request, Collection<Attachment> attachments) {
        Messaging messaging = new Messaging();
        messaging.getUserMessage().add(getUserMessage(request, attachments));
        return messaging;
    }

    public UserMessage getUserMessage(TransmissionRequest request, Collection<Attachment> attachments) {
        UserMessage userMessage = new UserMessage();
        userMessage.setMessageInfo(createMessageInfo(request));
        userMessage.setPartyInfo(createPartyInfo(request));
        userMessage.setCollaborationInfo(createCollaborationInfo(request));
        userMessage.setMessageProperties(createMessageProperties(request));
        userMessage.setPayloadInfo(createPayloadInfo(request, attachments));
        return userMessage;
    }

    private PayloadInfo createPayloadInfo(TransmissionRequest request, Collection<Attachment> attachments) {

        ArrayList<PartInfo> partInfos = Lists.newArrayList();
        for (Attachment attachment : attachments) {

            PartProperties partProperties = new PartProperties();

            String cid = "cid:" + AttachmentUtil.cleanContentId(attachment.getId());

            iteratorToStreamOfUnknownSize(attachment.getHeaderNames(), Spliterator.ORDERED, false)
                    .filter(header -> !"Content-ID".equals(header))
                    .map(header -> {
                        Property property = new Property();
                        property.setName(header);
                        property.setValue(attachment.getHeader(header));
                        return property;
                    })
                    .forEach(partProperties.getProperty()::add);

            if (request instanceof As4TransmissionRequest) {
                As4TransmissionRequest as4TransmissionRequest = (As4TransmissionRequest) request;
                if (null != as4TransmissionRequest.getPayloadCharset()) {
                    Property property = new Property();
                    property.setName("CharacterSet");
                    property.setValue(as4TransmissionRequest.getPayloadCharset().name().toLowerCase());
                    partProperties.getProperty().add(property);
                }

                if (null != as4TransmissionRequest.getCompressionType()) {
                    Property property = new Property();
                    property.setName("CompressionType");
                    property.setValue(as4TransmissionRequest.getCompressionType());
                    partProperties.getProperty().add(property);
                }
            }

            PartInfo partInfo = new PartInfo();
            partInfo.setHref(cid);
            partInfo.setPartProperties(partProperties);
            partInfos.add(partInfo);
        }

        PayloadInfo payloadInfo = new PayloadInfo();
        payloadInfo.getPartInfo().addAll(partInfos);
        return payloadInfo;
    }


    private MessageProperties createMessageProperties(TransmissionRequest request) {
        As4MessageProperties properties = new As4MessageProperties();

        if (request instanceof As4TransmissionRequest) {
            As4TransmissionRequest as4TransmissionRequest = (As4TransmissionRequest) request;
            if (as4TransmissionRequest.getMessageProperties() != null) {
                properties.addAll(as4TransmissionRequest.getMessageProperties());
            }
        }

        if (properties.isMissing("originalSender")) {
            properties.add(TransmissionRequestUtil.toAs4MessageProperty("originalSender", request.getHeader().getSender()));
        }

        if (properties.isMissing("finalRecipient")) {
            properties.add(TransmissionRequestUtil.toAs4MessageProperty("finalRecipient", request.getHeader().getReceiver()));
        }

        MessageProperties messageProperties = new MessageProperties();
        properties.stream()
                .map(p -> {
                    Property property = new Property();
                    property.setName(p.getName());
                    property.setType(p.getType());
                    property.setValue(p.getValue());
                    return property;
                })
                .forEach(messageProperties.getProperty()::add);
        return messageProperties;
    }

    private PartyInfo createPartyInfo(TransmissionRequest request) {

        String fromName = CertificateUtils.extractCommonName(certificate);
        String toName = CertificateUtils.extractCommonName(request.getEndpoint().getCertificate());

        PeppolConfiguration outboundConfiguration = request.getTag() instanceof PeppolConfiguration ?
                (PeppolConfiguration) request.getTag() : defaultOutboundConfiguration;


        PartyId fromPartyId = new PartyId();
        fromPartyId.setType(outboundConfiguration.getPartyIDType());
        fromPartyId.setValue(fromName);

        From from = new From();
        from.getPartyId().add(fromPartyId);
        from.setRole(outboundConfiguration.getFromRole());

        PartyId toPartyId = new PartyId();
        toPartyId.setType(outboundConfiguration.getPartyIDType());
        toPartyId.setValue(toName);

        To to = new To();
        to.getPartyId().add(toPartyId);
        to.setRole(outboundConfiguration.getToRole());

        PartyInfo partyInfo = new PartyInfo();
        partyInfo.setFrom(from);
        partyInfo.setTo(to);

        return partyInfo;
    }

    public CollaborationInfo createCollaborationInfo(TransmissionRequest request) {
        String action = actionProvider.getAction(request.getHeader().getDocumentType());

        ProcessIdentifier process = request.getHeader().getProcess();

        Service service = new Service();
        service.setType(process.getScheme().getIdentifier());
        service.setValue(process.getIdentifier());

        CollaborationInfo ci = new CollaborationInfo();
        ci.setConversationId(getConversationId(request));
        ci.setAction(action);
        ci.setService(service);

        if (request instanceof As4TransmissionRequest && ((As4TransmissionRequest) request).isPing()) {
            ci.setAction(TEST_ACTION);
            Service testActionService = new Service();
            testActionService.setValue(TEST_SERVICE);
            ci.setService(testActionService);
        }

        if (defaultOutboundConfiguration.getAgreementRef() != null) {
            AgreementRef agreementRef = new AgreementRef();
            agreementRef.setValue(defaultOutboundConfiguration.getAgreementRef());
            ci.setAgreementRef(agreementRef);
        }

        return ci;
    }

    private MessageInfo createMessageInfo(TransmissionRequest request) {
        GregorianCalendar gcal = GregorianCalendar.from(LocalDateTime.now().atZone(ZoneId.systemDefault()));
        XMLGregorianCalendar xmlDate;
        try {
            xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException("Error getting xml date", e);
        }

        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setMessageId(getMessageId(request));
        messageInfo.setTimestamp(xmlDate);

        if (request instanceof As4TransmissionRequest) {
            As4TransmissionRequest as4TransmissionRequest = (As4TransmissionRequest) request;
            if (as4TransmissionRequest.getRefToMessageId() != null) {
                messageInfo.setRefToMessageId(as4TransmissionRequest.getRefToMessageId());
            }
        }

        return messageInfo;
    }

    private String getMessageId(TransmissionRequest request) {
        String messageId = null;

        if (request instanceof As4TransmissionRequest) {
            As4TransmissionRequest as4TransmissionRequest = (As4TransmissionRequest) request;
            messageId = as4TransmissionRequest.getMessageId();
        }

        return messageId != null ? messageId : newId();
    }

    private String getConversationId(TransmissionRequest request) {
        String conversationId = null;

        if (request instanceof As4TransmissionRequest) {
            As4TransmissionRequest as4TransmissionRequest = (As4TransmissionRequest) request;
            conversationId = as4TransmissionRequest.getConversationId();
        }

        return conversationId != null ? conversationId : newId();
    }

    private String newId() {
        return messageIdGenerator.generate();
    }
}
