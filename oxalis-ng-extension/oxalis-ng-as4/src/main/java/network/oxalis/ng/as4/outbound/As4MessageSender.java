package network.oxalis.ng.as4.outbound;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.ng.as4.api.MessageIdGenerator;
import network.oxalis.ng.as4.common.AS4Constants;
import network.oxalis.ng.as4.common.MerlinProvider;
import network.oxalis.ng.as4.config.As4Conf;
import network.oxalis.ng.as4.lang.OxalisAs4TransmissionException;
import network.oxalis.ng.as4.util.CompressionUtil;
import network.oxalis.ng.as4.util.Constants;
import network.oxalis.ng.as4.util.PolicyService;
import network.oxalis.ng.api.outbound.TransmissionRequest;
import network.oxalis.ng.api.outbound.TransmissionResponse;
import network.oxalis.ng.api.settings.Settings;
import network.oxalis.ng.commons.http.HttpConf;
import network.oxalis.ng.commons.security.KeyStoreConf;
import org.apache.cxf.attachment.AttachmentUtil;
import org.apache.cxf.binding.soap.SoapHeader;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.headers.Header;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.jaxws.DispatchImpl;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.ws.policy.WSPolicyFeature;
import org.apache.wss4j.common.crypto.Merlin;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;

import jakarta.xml.bind.JAXBException;

import javax.net.ssl.KeyManager;
import javax.xml.namespace.QName;

import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Dispatch;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.soap.SOAPBinding;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.apache.cxf.rt.security.SecurityConstants.*;
import static org.apache.cxf.ws.security.SecurityConstants.USE_ATTACHMENT_ENCRYPTION_CONTENT_ONLY_TRANSFORM;

@Slf4j
public class As4MessageSender {

    public static final QName SERVICE_NAME = new QName("oxalis.network/", "outbound-service");
    public static final QName PORT_NAME = new QName("oxalis.network/", "port");

    private final MessagingProvider messagingProvider;
    private final MessageIdGenerator messageIdGenerator;
    private final Settings<KeyStoreConf> settings;
    private final Settings<As4Conf> as4settings;
    private final CompressionUtil compressionUtil;
    private final Settings<HttpConf> httpConfSettings;
    private final TransmissionResponseConverter transmissionResponseConverter;
    private final MerlinProvider merlinProvider;
    private final PolicyService policyService;
    private final String browserType;
    private final As4ReceiptValidationInInterceptor receiptValidationInInterceptor;

    @Inject
    public As4MessageSender(MessagingProvider messagingProvider, MessageIdGenerator messageIdGenerator, Settings<KeyStoreConf> settings, Settings<As4Conf> as4settings, CompressionUtil compressionUtil, Settings<HttpConf> httpConfSettings, TransmissionResponseConverter transmissionResponseConverter, MerlinProvider merlinProvider, PolicyService policyService, BrowserTypeProvider browserTypeProvider, As4ReceiptValidationInInterceptor receiptValidationInInterceptor) {
        this.messagingProvider = messagingProvider;
        this.messageIdGenerator = messageIdGenerator;
        this.settings = settings;
        this.as4settings = as4settings;
        this.compressionUtil = compressionUtil;
        this.httpConfSettings = httpConfSettings;
        this.transmissionResponseConverter = transmissionResponseConverter;
        this.merlinProvider = merlinProvider;
        this.policyService = policyService;
        this.browserType = browserTypeProvider.getBrowserType();
        this.receiptValidationInInterceptor = receiptValidationInInterceptor;
    }

    public TransmissionResponse send(TransmissionRequest request) throws OxalisAs4TransmissionException {
        DispatchImpl<SOAPMessage> dispatch = createDispatch(request);
        AttachmentHolder attachmentHolder = null;
        try {
            attachmentHolder = prepareAttachment(request);
            ArrayList<Attachment> attachments = new ArrayList<>(Collections.singletonList(attachmentHolder.attachment));
            dispatch.getRequestContext().put(Message.ATTACHMENTS, attachments);

            Messaging messaging = messagingProvider.createMessagingHeader(request, attachments);
            SoapHeader header = getSoapHeader(messaging);
            dispatch.getRequestContext().put(Header.HEADER_LIST, new ArrayList<>(Collections.singletonList(header)));
            return invoke(request, dispatch);
        } finally {
            if (attachmentHolder != null) {
                try {
                    attachmentHolder.inputStream.close();
                } catch (IOException e) {
                    log.error("Couldn't close attachment input stream", e);
                }
            }
        }
    }

    private TransmissionResponse invoke(TransmissionRequest request, DispatchImpl<SOAPMessage> dispatch) throws OxalisAs4TransmissionException {
        try {
            SOAPMessage response = dispatch.invoke(null);
            return transmissionResponseConverter.convert(request, response);
        } catch (Exception e) {
            throw new OxalisAs4TransmissionException("Failed to send message", e);
        }
    }

    private SoapHeader getSoapHeader(Messaging messaging) throws OxalisAs4TransmissionException {
        try {
            JAXBDataBinding binding = new JAXBDataBinding(Messaging.class);
            return new SoapHeader(
                    Constants.MESSAGING_QNAME,
                    messaging,
                    binding,
                    true);
        } catch (JAXBException e) {
            throw new OxalisAs4TransmissionException("Unable to marshal AS4 header", e);
        }
    }

    private void configureSecurity(TransmissionRequest request, Dispatch<SOAPMessage> dispatch) {
        Merlin merlin = merlinProvider.getMerlin();
        dispatch.getRequestContext().put(SIGNATURE_CRYPTO, merlin);
        dispatch.getRequestContext().put(SIGNATURE_PASSWORD, settings.getString(KeyStoreConf.KEY_PASSWORD));
        dispatch.getRequestContext().put(SIGNATURE_USERNAME, settings.getString(KeyStoreConf.KEY_ALIAS));
        dispatch.getRequestContext().put(ENCRYPT_CERT, request.getEndpoint().getCertificate());
        dispatch.getRequestContext().put(USE_ATTACHMENT_ENCRYPTION_CONTENT_ONLY_TRANSFORM, true);
    }

    public AttachmentHolder prepareAttachment(TransmissionRequest request) throws OxalisAs4TransmissionException {
        Map<String, List<String>> headers = new HashMap<>();

        headers.put("Content-ID", Collections.singletonList(getContentID(request)));
        headers.put("CompressionType", Collections.singletonList("application/gzip"));
        headers.put("MimeType", Collections.singletonList("application/xml"));

        try {
            InputStream compressedStream = compressionUtil.getCompressedStream(request.getPayload());
            Attachment attachment = AttachmentUtil.createAttachment(compressedStream, headers);

            return new AttachmentHolder(compressedStream, attachment);
        } catch (IOException e) {
            throw new OxalisAs4TransmissionException("Unable to compress payload", e);
        }
    }

    private String getContentID(TransmissionRequest request) {
        if (request instanceof As4TransmissionRequest) {
            As4TransmissionRequest as4request = (As4TransmissionRequest) request;
            return as4request.getPayloadHref();
        }

        return messageIdGenerator.generate();
    }

    private DispatchImpl<SOAPMessage> createDispatch(TransmissionRequest request) throws OxalisAs4TransmissionException {
        DispatchImpl<SOAPMessage> dispatch = (DispatchImpl<SOAPMessage>) getService(request)
                .createDispatch(PORT_NAME, SOAPMessage.class, Service.Mode.MESSAGE);
        dispatch.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, request.getEndpoint().getAddress().toString());

        configureSecurity(request, dispatch);

        TLSClientParameters tls = new TLSClientParameters();
        tls.setKeyManagers(new KeyManager[0]);   // no client cert, if in future bilateral test requires mTLS then set to null
        tls.setTrustManagers(null);              // JVM truststore
        //tls.setSecureSocketProtocol("TLSv1.2");// Not setting Protocol here, you can control it via -Djdk.tls.client.protocols=TLSv1.3,TLSv1.2

        final Client client = dispatch.getClient();

        // Add receipt validation interceptor for Peppol transmissions
        if (AS4Constants.PEPPOL.equalsIgnoreCase(as4settings.getString(As4Conf.TYPE))) {
            client.getInInterceptors().add(receiptValidationInInterceptor);
        }

        if (AS4Constants.CEF_CONFORMANCE.equalsIgnoreCase(as4settings.getString(As4Conf.TYPE))) {
            client.getInInterceptors().add(getLoggingBeforeSecurityInInterceptor());
        }

        final HTTPConduit httpConduit = (HTTPConduit) client.getConduit();
        final HTTPClientPolicy httpClientPolicy = httpConduit.getClient();
        httpClientPolicy.setConnectionTimeout(httpConfSettings.getInt(HttpConf.TIMEOUT_CONNECT));
        httpClientPolicy.setReceiveTimeout(httpConfSettings.getInt(HttpConf.TIMEOUT_READ));
        httpClientPolicy.setVersion(httpConfSettings.getString(HttpConf.HTTP_VERSION));
        httpClientPolicy.setAllowChunking(true);
        httpClientPolicy.setChunkLength(8192);
        httpClientPolicy.setBrowserType(browserType);
        httpConduit.setClient(httpClientPolicy);
        httpConduit.setTlsClientParameters(tls);

        return dispatch;
    }

    private LoggingBeforeSecurityInInterceptor getLoggingBeforeSecurityInInterceptor() {
        LoggingBeforeSecurityInInterceptor interceptor = new LoggingBeforeSecurityInInterceptor();
        interceptor.setPrettyLogging(true);
        interceptor.setLogMultipart(true);
        return interceptor;
    }

    private Service getService(TransmissionRequest request) throws OxalisAs4TransmissionException {
        Service service = Service.create(SERVICE_NAME, new LoggingFeature(), new WSPolicyFeature(policyService.getPolicy(request)));
        service.addPort(PORT_NAME, SOAPBinding.SOAP12HTTP_BINDING, request.getEndpoint().getAddress().toString());
        return service;
    }

    @RequiredArgsConstructor
    private static class AttachmentHolder {
        private final InputStream inputStream;
        private final Attachment attachment;
    }
}
