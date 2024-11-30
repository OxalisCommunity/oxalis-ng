package network.oxalis.as4.inbound;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.as4.lang.AS4Error;
import network.oxalis.as4.lang.OxalisAs4Exception;
import network.oxalis.as4.util.AS4ErrorCode;
import network.oxalis.as4.util.As4MessageFactory;
import network.oxalis.as4.util.MessageId;
import network.oxalis.api.model.TransmissionIdentifier;
import network.oxalis.api.persist.PersisterHandler;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.wss4j.common.ext.WSSecurityException;

import javax.xml.namespace.QName;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Singleton
public class As4FaultInHandler implements SOAPHandler<SOAPMessageContext> {

    private final As4MessageFactory as4MessageFactory;
    private final PersisterHandler persisterHandler;

    private static final String CERTIFICATE_ERROR_MSG = "Cannot find key for certificate";
    private static final String ERROR_CODE_FAILED_CHECK = "FAILED_CHECK";
    private static final String FAULT_CODE_FAILED_CHECK = "FailedCheck";

    @Inject
    public As4FaultInHandler(As4MessageFactory as4MessageFactory, PersisterHandler persisterHandler) {
        this.as4MessageFactory = as4MessageFactory;
        this.persisterHandler = persisterHandler;
    }

    @Override
    public Set<QName> getHeaders() {
        return Collections.emptySet();
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        String messageId = Optional.ofNullable((MessageId) context.get(MessageId.MESSAGE_ID))
                .map(MessageId::getValue)
                .orElse(null);

        Exception exception = (Exception) context.get(Exception.class.getName());

        if (exception == null) {
            return true;
        }

        log.info("handleFault for Exception", exception);

        AS4Error as4Error = toAS4Error(exception);

        handleAS4Error(context, messageId, as4Error);

        return true;
    }

    protected void handleAS4Error(SOAPMessageContext context, String messageId, AS4Error as4Error) {
        SOAPMessage errorMessage = as4MessageFactory.createErrorMessage(messageId, as4Error);
        context.setMessage(errorMessage);

        Path firstPayloadPath = (Path) context.get(AS4MessageContextKey.FIRST_PAYLOAD_PATH);
        As4PayloadHeader firstPayloadHeader = (As4PayloadHeader) context.get(AS4MessageContextKey.FIRST_PAYLOAD_HEADER);

        if (messageId != null && firstPayloadPath != null) {
            try {
                persisterHandler.persist(TransmissionIdentifier.of(messageId), firstPayloadHeader, firstPayloadPath, as4Error.getException());
            } catch (Exception e) {
                log.error("Unable to persist exception", e);
            }
        }
    }

    @Override
    public void close(MessageContext context) {
        // Intentionally left empty
    }

    public static AS4Error toAS4Error(Throwable t) {
        // Is there a better way of getting the inMessage using JAX-WS?
        Optional<Message> faultMessage = Optional.ofNullable(PhaseInterceptorChain.getCurrentMessage());
        Optional<Message> inMessage = faultMessage
                .map(Message::getExchange)
                .map(Exchange::getInMessage);

        if (t instanceof Fault) {
            Fault fault = (Fault) t;
            t = fault.getCause();
        }

        if (t instanceof WebServiceException) {
            WebServiceException webServiceException = (WebServiceException) t;
            t = webServiceException.getCause();
        }

        if (t instanceof WSSecurityException && inMessage.isPresent()) {

            boolean IsSecurityException = false;
            String detailSecurityExceptionMessage = "";

            if(null != t.getMessage()) {
                detailSecurityExceptionMessage = t.getMessage();
            }

            if(null != ((WSSecurityException) t).getErrorCode()) {
                String errorCode = ((WSSecurityException) t).getErrorCode().name();
                IsSecurityException = errorCode.equalsIgnoreCase(ERROR_CODE_FAILED_CHECK);
            }

            if(null != ((WSSecurityException) t).getFaultCode()) {
                String faultCode = (null == ((WSSecurityException) t).getFaultCode().getLocalPart() ? "" : ((WSSecurityException) t).getFaultCode().getLocalPart());
                IsSecurityException = faultCode.equalsIgnoreCase(FAULT_CODE_FAILED_CHECK);
            }

            if(IsSecurityException || detailSecurityExceptionMessage.equalsIgnoreCase(CERTIFICATE_ERROR_MSG)) {
                return new OxalisAs4Exception("PEPPOL:NOT_SERVICED", AS4ErrorCode.EBMS_0004, AS4ErrorCode.Severity.FAILURE);
            }

            boolean isCompressionError = (boolean) inMessage.get().getOrDefault("oxalis.as4.compressionErrorDetected", false);
            if (isCompressionError) {

                return new OxalisAs4Exception(
                        "Content cannot be compressed after signature/encryption", AS4ErrorCode.EBMS_0303);
            }

            return new OxalisAs4Exception(t.getMessage(), t, AS4ErrorCode.EBMS_0009, AS4ErrorCode.Severity.ERROR);
        }

        if (t instanceof AS4Error) {
            return (AS4Error) t;
        }

        return new OxalisAs4Exception(t.getMessage(), t, AS4ErrorCode.EBMS_0004, AS4ErrorCode.Severity.ERROR);
    }
}
