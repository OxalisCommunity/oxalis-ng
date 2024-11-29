package network.oxalis.outbound.transmission;

import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import network.oxalis.api.outbound.TransmissionMessage;
import network.oxalis.api.outbound.TransmissionRequest;
import network.oxalis.as4.common.DefaultMessageIdGenerator;
import network.oxalis.as4.util.PeppolConfiguration;
import network.oxalis.commons.header.SbdhHeaderParser;
import network.oxalis.commons.tag.NoopTagGenerator;
import network.oxalis.commons.transformer.NoopContentDetector;
import network.oxalis.commons.transformer.NoopContentWrapper;
import network.oxalis.vefa.peppol.common.model.Endpoint;
import network.oxalis.vefa.peppol.common.model.TransportProfile;
import org.apache.cxf.attachment.AttachmentUtil;
import org.apache.cxf.message.Attachment;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Property;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Service;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.UserMessage;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.X509Certificate;
import java.util.*;

public abstract class AbstractMessagingProviderTest {

    private static final String Content_ID_KEY = "Content-ID";
    private static final String Content_ID_VALUE = "payloadId";
    private static final String COMPRESSION_TYPE_KEY = "CompressionType";
    private static final String COMPRESSION_TYPE_VALUE = "application/gzip";
    private static final String MIME_TYPE_KEY = "MimeType";
    private static final String MIME_TYPE_VALUE = "application/xml";
    private static final String SENDER = "OxalisSender";
    private static final String RECEIVER = "OxalisReceiver";
    private static final String TO_ROLE  = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder";
    private static final String FROM_ROLE = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator";

    private final String ACTION = getAction();
    private final String SERVICE_TYPE = getServiceType();
    private final String SERVICE_VALUE = getServiceValue();
    private final String PARTY_TYPE = getPartyType();

    private final String FINAL_RECIPIENT = getFinalRecipient();
    private final String ORIGINAL_SENDER = getOriginalSender();


    private MessagingProviderFacade messagingProvider;
    private X509Certificate senderCert;
    private X509Certificate receiverCert;


    protected abstract String getPayloadPath();

    protected abstract PeppolConfiguration getPEPPOLOutboundConfiguration();

    protected abstract String getAction();
    protected abstract String getServiceType();
    protected abstract String getServiceValue();
    protected abstract String getPartyType();
    protected abstract String getFinalRecipient();
    protected abstract String getOriginalSender();


    @BeforeClass
    public void beforeClass() throws Exception {

        senderCert = generateSelfSignedCertificate("CN=" + SENDER + ",O=Difi,L=Oslo,C=NO");
        receiverCert = generateSelfSignedCertificate("CN=" + RECEIVER + ",O=Difi,L=Oslo,C=NO");

        messagingProvider = new MessagingProviderFacade(
                senderCert,
                new DefaultMessageIdGenerator("test"),
                getPEPPOLOutboundConfiguration());
    }



    @Test
    public void testCreateMessagingHeader() throws Exception {

        TransmissionRequestFactory transmissionRequestFactory = new TransmissionRequestFactory(
                new NoopContentDetector(),
                new NoopContentWrapper(),
                new NoopTagGenerator(),
                new SbdhHeaderParser(),
                new Tracer() {
                    @Override
                    public SpanBuilder spanBuilder(String spanName) {
                        return null;
                    }
                }
        );

        TransmissionMessage transmissionMessage;
        try (InputStream inputStream = getClass().getResourceAsStream(getPayloadPath())) {
            transmissionMessage = transmissionRequestFactory.newInstance(inputStream);
        }


        Assert.assertNotNull(transmissionMessage.getHeader());

        TransmissionRequest transmissionRequest = new DefaultTransmissionRequestFacade(
                transmissionMessage,
                Endpoint.of(TransportProfile.PEPPOL_AS4_2_0, null, receiverCert)
        );

        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put(Content_ID_KEY, Collections.singletonList(Content_ID_VALUE));
        headers.put(COMPRESSION_TYPE_KEY, Collections.singletonList(COMPRESSION_TYPE_VALUE));
        headers.put(MIME_TYPE_KEY, Collections.singletonList(MIME_TYPE_VALUE));

        Attachment attachment = AttachmentUtil.createAttachment(transmissionRequest.getPayload(), headers);

        Messaging messaging = messagingProvider.createMessagingHeader(
                transmissionRequest , new ArrayList<>(Collections.singletonList(attachment))
        );

        UserMessage userMessage = messaging.getUserMessage().get(0);

        // MessageInfo
        XMLGregorianCalendar timestamp = userMessage.getMessageInfo().getTimestamp();
        String messageId = userMessage.getMessageInfo().getMessageId();

        Assert.assertNotNull(timestamp);
        Assert.assertNotNull(messageId);


        // PartyInfo
        String to = userMessage.getPartyInfo().getTo().getPartyId().get(0).getValue();
        String toType = userMessage.getPartyInfo().getTo().getPartyId().get(0).getType();
        String toRole = userMessage.getPartyInfo().getTo().getRole();
        String from = userMessage.getPartyInfo().getFrom().getPartyId().get(0).getValue();
        String fromType = userMessage.getPartyInfo().getFrom().getPartyId().get(0).getType();
        String fromRole = userMessage.getPartyInfo().getFrom().getRole();

        Assert.assertEquals(to, RECEIVER);
        Assert.assertEquals(toType, PARTY_TYPE);
        Assert.assertEquals(toRole, TO_ROLE);
        Assert.assertEquals(from, SENDER);
        Assert.assertEquals(fromType, PARTY_TYPE);
        Assert.assertEquals(fromRole, FROM_ROLE);


        // CollaborationInfo
        String action = userMessage.getCollaborationInfo().getAction();
        Service service = userMessage.getCollaborationInfo().getService();
        String conversationId = userMessage.getCollaborationInfo().getConversationId();

        Assert.assertEquals(action, ACTION);
        Assert.assertEquals(service.getType(), SERVICE_TYPE);
        Assert.assertEquals(service.getValue(), SERVICE_VALUE);
        Assert.assertNotNull(conversationId);


        // MessageProperties
        String finalRecipient = userMessage.getMessageProperties().getProperty().stream()
                .filter(p -> "finalRecipient".equalsIgnoreCase(p.getName()))
                .map(Property::getValue)
                .findFirst()
                .get();
        String originalSender = userMessage.getMessageProperties().getProperty().stream()
                .filter(p -> "originalSender".equalsIgnoreCase(p.getName()))
                .map(Property::getValue)
                .findFirst()
                .get();

        Assert.assertEquals(finalRecipient, FINAL_RECIPIENT);
        Assert.assertEquals(originalSender, ORIGINAL_SENDER);


        // PayloadInfo
        int payloadCount = userMessage.getPayloadInfo().getPartInfo().size();
        Assert.assertEquals(payloadCount, 1);

        List<Property> partInfo = userMessage.getPayloadInfo().getPartInfo().get(0).getPartProperties().getProperty();
        String compressionType = partInfo.stream()
                .filter(pi -> COMPRESSION_TYPE_KEY.equalsIgnoreCase(pi.getName()))
                .map(Property::getValue)
                .findFirst()
                .get();
        String mimeType = partInfo.stream()
                .filter(pi -> MIME_TYPE_KEY.equalsIgnoreCase(pi.getName()))
                .map(Property::getValue)
                .findFirst()
                .get();
        String contentID = userMessage.getPayloadInfo().getPartInfo().get(0).getHref();

        Assert.assertEquals(compressionType, COMPRESSION_TYPE_VALUE);
        Assert.assertEquals(mimeType, MIME_TYPE_VALUE);
        Assert.assertEquals(contentID, "cid:" + Content_ID_VALUE);
    }

    public static X509Certificate generateSelfSignedCertificate(String subjectDN) throws Exception
    {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair kp = keyPairGenerator.generateKeyPair();


        X500Name dnName = new X500Name(subjectDN);
        BigInteger certSerialNumber = new BigInteger(Long.toString(System.currentTimeMillis()));


        Date startDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.YEAR, 1); // <-- 1 Yr validity

        Date endDate = calendar.getTime();

        JcaX509v3CertificateBuilder cb = new JcaX509v3CertificateBuilder(dnName, certSerialNumber, startDate, endDate, dnName, kp.getPublic());

        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSA").build(kp.getPrivate());


        return new JcaX509CertificateConverter().getCertificate(cb.build(contentSigner));
    }
}
