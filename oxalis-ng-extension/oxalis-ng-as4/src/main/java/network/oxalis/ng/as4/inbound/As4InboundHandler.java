package network.oxalis.ng.as4.inbound;

import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.ng.api.header.HeaderParser;
import network.oxalis.ng.api.inbound.InboundService;
import network.oxalis.ng.api.lang.TimestampException;
import network.oxalis.ng.api.lang.VerifierException;
import network.oxalis.ng.api.model.Direction;
import network.oxalis.ng.api.model.TransmissionIdentifier;
import network.oxalis.ng.api.persist.PersisterHandler;
import network.oxalis.ng.api.timestamp.Timestamp;
import network.oxalis.ng.api.timestamp.TimestampProvider;
import network.oxalis.ng.api.transmission.TransmissionVerifier;
import network.oxalis.ng.as4.common.As4MessageProperties;
import network.oxalis.ng.as4.common.As4MessageProperty;
import network.oxalis.ng.as4.lang.OxalisAs4Exception;
import network.oxalis.ng.as4.lang.OxalisAs4TransmissionException;
import network.oxalis.ng.as4.util.*;
import network.oxalis.ng.commons.header.SbdhHeaderParser;
import network.oxalis.ng.commons.io.UnclosableInputStream;
import network.oxalis.ng.commons.mode.OxalisCertificateValidator;
import network.oxalis.ng.as4.util.*;
import network.oxalis.vefa.peppol.common.code.DigestMethod;
import network.oxalis.vefa.peppol.common.code.Service;
import network.oxalis.vefa.peppol.common.model.*;
import network.oxalis.vefa.peppol.sbdh.SbdReader;
import network.oxalis.vefa.peppol.sbdh.lang.SbdhException;
import network.oxalis.vefa.peppol.security.lang.PeppolSecurityException;
import org.apache.cxf.attachment.AttachmentUtil;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.ws.policy.AssertionInfoMap;
import org.apache.neethi.Policy;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.*;
import org.w3.xmldsig.ReferenceType;

import jakarta.xml.soap.*;
import jakarta.xml.ws.handler.MessageContext;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

@Slf4j
@Singleton
public class As4InboundHandler {

    private static final String REQUIRED_PAYLOAD_HREF_PREFIX = "cid:";
    public static final String COMPRESSION_TYPE = "CompressionType";

    private final TransmissionVerifier transmissionVerifier;
    private final PersisterHandler persisterHandler;
    private final TimestampProvider timestampProvider;
    private final HeaderParser headerParser;
    private final As4MessageFactory as4MessageFactory;
    private final PolicyService policyService;
    private final InboundService inboundService;
    private final OxalisCertificateValidator certificateValidator;

    @Inject
    public As4InboundHandler(TransmissionVerifier transmissionVerifier, PersisterHandler persisterHandler,
                             TimestampProvider timestampProvider, HeaderParser headerParser, As4MessageFactory as4MessageFactory,
                             PolicyService policyService, InboundService inboundService, OxalisCertificateValidator certificateValidator) {
        this.transmissionVerifier = transmissionVerifier;
        this.persisterHandler = persisterHandler;
        this.timestampProvider = timestampProvider;
        this.headerParser = headerParser;
        this.as4MessageFactory = as4MessageFactory;
        this.policyService = policyService;
        this.inboundService = inboundService;
        this.certificateValidator = certificateValidator;
    }

    public SOAPMessage handle(SOAPMessage request, MessageContext messageContext) throws OxalisAs4Exception {
        SOAPHeader soapHeader = getSoapHeader(request);

        X509Certificate senderCertificate = getSenderCertificate(soapHeader);

        Timestamp timestamp = getTimestamp(soapHeader);
        Iterator<AttachmentPart> attachments = CastUtils.cast(request.getAttachments());

        // Organize input data
        UserMessage userMessage = SOAPHeaderParser.getUserMessage(soapHeader);

        As4EnvelopeHeader envelopeHeader = parseAs4EnvelopeHeader(userMessage);
        messageContext.put(AS4MessageContextKey.ENVELOPE_HEADER, envelopeHeader);

        TransmissionIdentifier messageId = TransmissionIdentifier.of(envelopeHeader.getMessageId());

        validateMessageId(messageId.getIdentifier()); // Validate UserMessage

        try {
            certificateValidator.validate(Service.AP, senderCertificate);
        } catch (PeppolSecurityException peppolSecurityException) {
            throw new OxalisAs4Exception("PEPPOL:NOT_SERVICED", AS4ErrorCode.EBMS_0004, AS4ErrorCode.Severity.FAILURE);
        }

        validatePayloads(userMessage.getPayloadInfo()); // Validate Payloads

        List<ReferenceType> referenceList = SOAPHeaderParser.getReferenceListFromSignedInfo(soapHeader);
        ProsessingContext prosessingContext = new ProsessingContext(timestamp, referenceList);

        // Prepare response
        SOAPMessage response = as4MessageFactory.createReceiptMessage(userMessage, prosessingContext);

        if (!isPingMessage(userMessage)) {
            // Inform Backend

            // Take a copy of the response so that we can persist it as metadata/proof
            byte[] copyOfReceipt = copyReceipt(response);

            // Handle payload
            LinkedHashMap<InputStream, As4PayloadHeader> payloads = parseAttachments(attachments, userMessage);

            List<Path> paths = new ArrayList<>();
            for (Map.Entry<InputStream, As4PayloadHeader> payload : payloads.entrySet()) {
                validateAttachmentHeader(payload.getValue());

                // Persist payload
                paths.add(persistPayload(payload.getKey(), payload.getValue(), messageId));
            }

            Path firstPayloadPath = paths.get(0);
            messageContext.put(AS4MessageContextKey.FIRST_PAYLOAD_PATH, firstPayloadPath);
            messageContext.put(AS4MessageContextKey.FIRST_PAYLOAD_HEADER, payloads.values().iterator().next());

            // Persist Metadata
            As4PayloadHeader firstHeader = payloads.entrySet().iterator().next().getValue();
            String firstAttachmentId = envelopeHeader.getPayloadCIDs().get(0);
            Digest firstAttachmentDigest = Digest.of(DigestMethod.SHA256, SOAPHeaderParser.getAttachmentDigest(firstAttachmentId, soapHeader));

            As4InboundMetadata as4InboundMetadata = new As4InboundMetadata(
                    messageId,
                    userMessage.getCollaborationInfo().getConversationId(),
                    firstHeader,
                    timestamp,
                    TransportProfile.AS4,
                    firstAttachmentDigest,
                    senderCertificate,
                    copyOfReceipt,
                    envelopeHeader);

            try {
                persisterHandler.persist(as4InboundMetadata, firstPayloadPath);
            } catch (IOException e) {
                throw new OxalisAs4Exception("Error persisting AS4 metadata", e, AS4ErrorCode.EBMS_0202);
            }

            messageContext.put(AS4MessageContextKey.PERSISTED, true);

            // Persist statistics
            inboundService.complete(as4InboundMetadata);
        }

        // Send response
        Policy policy = null;
        try {
            policy = policyService.getPolicy(userMessage.getCollaborationInfo());
        } catch (OxalisAs4TransmissionException e) {
            throw new OxalisAs4Exception("Could not get policy", e, AS4ErrorCode.EBMS_0202);
        }

        try {
            response.setProperty(AssertionInfoMap.class.getName(), new AssertionInfoMap(policy));
            response.saveChanges();
        } catch (SOAPException e) {
            throw new OxalisAs4Exception("Error persisting AS4 metadata", e, AS4ErrorCode.EBMS_0202);
        }

        return response;
    }

    private X509Certificate getSenderCertificate(SOAPHeader soapHeader) throws OxalisAs4Exception {
        try {
            return SOAPHeaderParser.getSenderCertificate(soapHeader);
        } catch (OxalisAs4Exception e) {
            throw new OxalisAs4Exception("PEPPOL:NOT_SERVICED", AS4ErrorCode.EBMS_0004, AS4ErrorCode.Severity.FAILURE);
        }
    }

    private boolean isPingMessage(UserMessage userMessage) {
        if (userMessage == null) {
            return false;
        }

        CollaborationInfo collaborationInfo = userMessage.getCollaborationInfo();

        if (collaborationInfo == null) {
            return false;
        }

        return Optional.ofNullable(collaborationInfo.getService())
                .map(org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Service::getValue)
                .map(service -> Optional.ofNullable(collaborationInfo.getAction())
                        .map(action ->
                                Constants.TEST_SERVICE.equals(service) && Constants.TEST_ACTION.equals(action)
                        ).orElse(false)
                ).orElse(false);
    }

    public static void validatePayloads(PayloadInfo payloadInfo) throws OxalisAs4Exception {
        List<String> externalPayloads = payloadInfo.getPartInfo().stream()
                .map(PartInfo::getHref)
                .filter(href -> !href.startsWith(REQUIRED_PAYLOAD_HREF_PREFIX))
                .collect(Collectors.toList());

        if (!externalPayloads.isEmpty()) {
            String errorMessage = "Invalid PayloadInfo. Href(s) detected with \"external\" source: " + externalPayloads;
            log.debug(errorMessage);

            throw new OxalisAs4Exception(
                    errorMessage,
                    AS4ErrorCode.EBMS_0009
            );
        }


        List<String> payloadsWithInvalidCharset = payloadInfo.getPartInfo().stream()
                .filter(As4InboundHandler::partInfoHasInvalidCharset)
                .map(PartInfo::getHref)
                .collect(Collectors.toList());

        if (!payloadsWithInvalidCharset.isEmpty()) {
            String errorMessage = "Invalid PayloadInfo. Part(s) detected invalid \"CharacterSet\" header: " + payloadsWithInvalidCharset;
            log.debug(errorMessage);

            throw new OxalisAs4Exception(
                    errorMessage,
                    AS4ErrorCode.EBMS_0009
            );
        }


        List<String> payloadsMissingMimeTypeHeader = payloadInfo.getPartInfo().stream()
                .filter(As4InboundHandler::partInfoMissingMimeTypeHeader)
                .map(PartInfo::getHref)
                .collect(Collectors.toList());

        if (!payloadsMissingMimeTypeHeader.isEmpty()) {
            String errorMessage = "Invalid PayloadInfo. Part(s) detected without \"MimeType\" header: " + payloadsMissingMimeTypeHeader;
            log.debug(errorMessage);

            throw new OxalisAs4Exception(
                    errorMessage,
                    AS4ErrorCode.EBMS_0009
            );
        }

    }

    private static boolean partInfoHasInvalidCharset(PartInfo partInfo) {

        return Optional.ofNullable(partInfo)
                .map(PartInfo::getPartProperties)
                .map(PartProperties::getProperty)
                .map(Collection::stream).orElse(Stream.empty())
                .anyMatch(property ->
                        Optional.of(property)
                                .map(Property::getName)
                                .filter("CharacterSet"::equals)
                                .map(fieldName -> Optional.of(property)
                                        .map(Property::getValue)
                                        .map(charset -> {
                                            try {
                                                return null == Charset.forName(property.getValue());
                                            } catch (Exception e) {
                                                return true;
                                            }
                                        }).orElse(true)
                                ).orElse(false)
                );
    }

    public static boolean partInfoMissingMimeTypeHeader(PartInfo partInfo) {
        return Optional.ofNullable(partInfo)
                .map(PartInfo::getPartProperties)
                .map(PartProperties::getProperty)
                .map(Collection::stream).orElse(Stream.empty())
                .map(Property::getName)
                .noneMatch("MimeType"::equals);
    }

    public byte[] copyReceipt(SOAPMessage response) throws OxalisAs4Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            response.writeTo(bos);
        } catch (SOAPException | IOException e) {
            throw new OxalisAs4Exception("Could not write SOAP response", e, AS4ErrorCode.EBMS_0202);
        }

        return bos.toByteArray();
    }

    public void validateMessageId(String messageId) throws OxalisAs4Exception {

        if (!MessageIdUtil.verify(messageId)) {
            throw new OxalisAs4Exception(
                    "Invalid Message-ID '" + messageId + "' in inbound message.",
                    AS4ErrorCode.EBMS_0009
            );
        }

    }

    private LinkedHashMap<InputStream, As4PayloadHeader> parseAttachments(Iterator<AttachmentPart> attachments, UserMessage userMessage) throws OxalisAs4Exception {

        if (!attachments.hasNext()) {
            throw new OxalisAs4Exception("No attachment(s) present");
        }

        // <ContentId, <HeaderName, MimeHeader>>
        Map<String, Map<String, MimeHeader>> partInfoHeadersMap = userMessage.getPayloadInfo().getPartInfo().stream()
                .collect(
                        Collectors.toMap(
                                partInfo -> AttachmentUtil.cleanContentId(partInfo.getHref()),
                                partInfo -> partInfo.getPartProperties().getProperty().stream().collect(
                                        Collectors.toMap(
                                                Property::getName,
                                                property -> new MimeHeader(property.getName(), property.getValue())
                                        )
                                )
                        )
                );

        LinkedHashMap<InputStream, As4PayloadHeader> payloads = new LinkedHashMap<>();

        Collection<Attachment> s = PhaseInterceptorChain.getCurrentMessage().getAttachments();

        for (Attachment attachment : s) {
            try {
                InputStream is = attachment.getDataHandler().getInputStream();
                String contentId = AttachmentUtil.cleanContentId(attachment.getId());

                Map<String, MimeHeader> mimeHeaders = new HashMap<>();
                attachment.getHeaderNames()
                        .forEachRemaining(h -> mimeHeaders.put(h, new MimeHeader(h, attachment.getHeader(h))));

                Map<String, MimeHeader> partInfoHeaders = partInfoHeadersMap.get(contentId);

                if (isAttachmentCompressed(partInfoHeaders, mimeHeaders)) {
                    try {
                        is = new GZIPInputStream(new BufferedInputStream(is), 8192);
                    } catch (IOException e) {
                        log.info("PartInfo headers: {}", partInfoHeaders.values().stream()
                                .map(p -> p.getName() + "=" + p.getValue())
                                .collect(Collectors.joining(", ", "{", "}")));

                        log.info("MIME headers: {}", mimeHeaders.values().stream()
                                .map(p -> p.getName() + "=" + p.getValue())
                                .collect(Collectors.joining(", ", "{", "}")));

                        throw new OxalisAs4Exception(
                                "Unable to initiate decompression of payload with Content-ID: " + contentId,
                                e,
                                AS4ErrorCode.EBMS_0303,
                                AS4ErrorCode.Severity.FAILURE
                        );
                    }
                }

                BufferedInputStream bis = new BufferedInputStream(is, 65536);

                Header sbdh;
                if (headerParser instanceof SbdhHeaderParser) {
                    bis.mark(65536);
                    sbdh = readHeader(contentId, bis);
                    bis.reset();
                } else {
                    sbdh = new Header()
                            .sender(ParticipantIdentifier.of(userMessage.getPartyInfo().getFrom().getPartyId().get(0).getValue()))
                            .receiver(ParticipantIdentifier.of(userMessage.getPartyInfo().getTo().getPartyId().get(0).getValue()))
                            .documentType(DocumentTypeIdentifier.of(userMessage.getCollaborationInfo().getService().getValue(), Scheme.of(userMessage.getCollaborationInfo().getService().getType())))
                            .identifier(InstanceIdentifier.of(userMessage.getCollaborationInfo().getAction()));
                }

                // Get an "unexpected eof in prolog"
                As4PayloadHeader header = new As4PayloadHeader(sbdh, partInfoHeaders.values(), contentId, userMessage.getMessageInfo().getMessageId());

                // Extract "fresh" InputStream
                payloads.put(bis, header);

            } catch (IOException e) {
                throw new OxalisAs4Exception("Could not get attachment input stream", e);
            }
        }

        return payloads;
    }

    private Header readHeader(String contentId, InputStream is) throws OxalisAs4Exception {
        try (SbdReader sbdReader = SbdReader.newInstance(is)) {
            return sbdReader.getHeader();
        } catch (SbdhException | IOException e) {
            launderZipException(contentId, e);
            throw new OxalisAs4Exception("Could not extract SBDH from payload");
        }
    }

    private void launderZipException(String contentId, Exception e) throws OxalisAs4Exception {
        Throwable cause = e;
        for (int i = 0; i < 10 && cause != null; i++) {
            if (cause instanceof ZipException) {
                throw new OxalisAs4Exception(
                        "Unable to decompress of payload with Content-ID: " + contentId,
                        cause,
                        AS4ErrorCode.EBMS_0303,
                        AS4ErrorCode.Severity.FAILURE
                );
            }

            cause = e.getCause();
        }
    }

    private boolean isAttachmentCompressed(Map<String, MimeHeader> partInfoHeaders, Map<String, MimeHeader> mimeHeaders) {
        if (partInfoHeaders.containsKey(COMPRESSION_TYPE)) {
            String value = partInfoHeaders.get(COMPRESSION_TYPE).getValue();

            // Somehow this fails
            if ("application/gzip".equals(value)) {
                return true;
            }
        }

//        if (mimeHeaders.containsKey(COMPRESSION_TYPE) &&
//                "application/gzip".equals(mimeHeaders.get(COMPRESSION_TYPE).getValue())) {
//            return true;
//        }

        return false;
    }

    private As4EnvelopeHeader parseAs4EnvelopeHeader(UserMessage userMessage) {

        As4EnvelopeHeader as4EnvelopeHeader = new As4EnvelopeHeader();

        as4EnvelopeHeader.setMessageId(userMessage.getMessageInfo().getMessageId());
        as4EnvelopeHeader.setConversationId(userMessage.getCollaborationInfo().getConversationId());

        as4EnvelopeHeader.setFromPartyId(userMessage.getPartyInfo().getFrom().getPartyId().stream().map(PartyId::getValue).collect(Collectors.toList()));
        as4EnvelopeHeader.setFromPartyRole(userMessage.getPartyInfo().getFrom().getRole());

        as4EnvelopeHeader.setToPartyId(userMessage.getPartyInfo().getTo().getPartyId().stream().map(PartyId::getValue).collect(Collectors.toList()));
        as4EnvelopeHeader.setToPartyRole(userMessage.getPartyInfo().getTo().getRole());

        as4EnvelopeHeader.setAction(userMessage.getCollaborationInfo().getAction());
        as4EnvelopeHeader.setService(userMessage.getCollaborationInfo().getService().getValue());

        as4EnvelopeHeader.setMessageProperties(userMessage.getMessageProperties().getProperty()
                .stream()
                .map(p -> new As4MessageProperty(p.getName(), p.getType(), p.getValue()))
                .collect(Collectors.toCollection(As4MessageProperties::new)));

        as4EnvelopeHeader.setPayloadCIDs(userMessage.getPayloadInfo().getPartInfo().stream().map(PartInfo::getHref).collect(Collectors.toList()));

        return as4EnvelopeHeader;
    }

    private Timestamp getTimestamp(SOAPHeader header) throws OxalisAs4Exception {
        byte[] signature = SOAPHeaderParser.getSignature(header);
        try {
            return timestampProvider.generate(signature, Direction.IN);
        } catch (TimestampException e) {
            throw new OxalisAs4Exception("Error generating timestamp", e);
        }
    }

    private Path persistPayload(InputStream inputStream, As4PayloadHeader as4PayloadHeader, TransmissionIdentifier ti) throws OxalisAs4Exception {
        try (InputStream is = inputStream) {
            // Persist content
            Path payloadPath = persisterHandler.persist(ti, as4PayloadHeader, new UnclosableInputStream(is));

            // Exhaust InputStream
            ByteStreams.exhaust(is);
            inputStream.close();
            return payloadPath;
        } catch (IOException e) {
            launderZipException(as4PayloadHeader.getCid(), e);
            throw new OxalisAs4Exception("Error processing payload input stream", e);
        }
    }

    private void validateAttachmentHeader(Header attachmentHeader) throws OxalisAs4Exception {
        try {
            transmissionVerifier.verify(attachmentHeader, Direction.IN);
        } catch (VerifierException e) {
            throw new OxalisAs4Exception("Error verifying SBDH", e);
        }
    }

    private SOAPHeader getSoapHeader(SOAPMessage request) throws OxalisAs4Exception {
        try {
            return request.getSOAPHeader();
        } catch (SOAPException e) {
            throw new OxalisAs4Exception("Could not get SOAP header", e);
        }
    }
}
