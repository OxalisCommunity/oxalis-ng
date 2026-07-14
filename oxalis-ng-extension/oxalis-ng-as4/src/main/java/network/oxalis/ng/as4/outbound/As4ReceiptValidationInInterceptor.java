package network.oxalis.ng.as4.outbound;

import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.ng.api.settings.Settings;
import network.oxalis.ng.as4.config.As4Conf;
import network.oxalis.ng.as4.lang.OxalisAs4Exception;
import network.oxalis.ng.as4.lang.OxalisAs4TransmissionException;
import network.oxalis.ng.as4.util.AS4ErrorCode;
import network.oxalis.ng.as4.util.Constants;
import network.oxalis.ng.as4.util.PeppolAs4ReceiptValidator;
import network.oxalis.ng.as4.util.SchemaValidatorFactory;
import network.oxalis.ng.as4.util.SchemaValidatorFactory.DefaultValidationErrorHandler;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Validator;
import java.io.IOException;

/**
 * CXF interceptor that validates AS4 receipt messages in the outbound response chain.
 *
 * <p>This interceptor performs two-tier validation of AS4 receipts received as responses to user messages:
 * <ol>
 *   <li><b>Schema Validation:</b> Validates against core AS4 schemas (SOAP 1.2, ebMS v3, EBBP signals, XMLDsig)</li>
 *   <li><b>Conformance Validation:</b> Validates PEPPOL-specific requirements (e.g., NonRepudiationInformation presence)</li>
 * </ol>
 *
 * <p><b>Validation Modes:</b>
 * <ul>
 *   <li><b>NONE:</b> Validation completely skipped </li>
 *   <li><b>LOGGING (default):</b> Validation failures logged as warnings, processing continues</li>
 *   <li><b>STRICT:</b> Validation failures throw Fault, terminating the exchange</li>
 * </ul>
 *
 * <p><b>Configuration:</b>
 * Set mode via {@code oxalis.as4.receipt.validation} property:
 * <pre>
 * oxalis.as4.receipt.validation=STRICT  # Fail fast on invalid receipts
 * oxalis.as4.receipt.validation=LOGGING # Log issues but continue (default)
 * oxalis.as4.receipt.validation=NONE    # Skip validation entirely
 * </pre>
 *
 * @see SchemaValidatorFactory for schema-level validation
 * @see PeppolAs4ReceiptValidator for PEPPOL conformance rules
 */

@Slf4j
public class As4ReceiptValidationInInterceptor extends AbstractPhaseInterceptor<SoapMessage> {

    // Validation mode determines how interceptor responds on validation failures (default: LOGGING)
    private final ValidationMode validationMode;


    /**
     * Validation mode determines how interceptor responds on validation failures (default: LOGGING)
     */
    public enum ValidationMode {
        NONE,      // Skip validation
        LOGGING,   // Log errors but continue
        STRICT;     // Throw exception on errors and prevent further processing

        public static ValidationMode parseValidationMode(Settings<As4Conf> as4Settings) {

            String modeStr = as4Settings.getString(As4Conf.RECEIPT_VALIDATION);

            if (modeStr == null) {
                return ValidationMode.LOGGING;
            }

            try {
                return ValidationMode.valueOf(modeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Unknown validation mode '{}', defaulting to LOGGING", modeStr);
                return ValidationMode.LOGGING;
            }
        }
    }

    /**
     * Creates an interceptor with validation mode parsed from AS4 settings.
     * Runs in {@link Phase#POST_PROTOCOL} by default.
     *
     * @param as4Settings configuration settings containing validation mode
     */
    public As4ReceiptValidationInInterceptor(Settings<As4Conf> as4Settings) {
        this(Phase.POST_PROTOCOL, ValidationMode.parseValidationMode(as4Settings));
    }

    /**
     * Creates an interceptor with explicit phase and validation mode.
     * Used primarily for testing or custom configuration scenarios, if required.
     *
     * @param phase          CXF interceptor phase (typically {@link Phase#POST_PROTOCOL})
     * @param validationMode how to handle validation failures
     */
    public As4ReceiptValidationInInterceptor(String phase, ValidationMode validationMode) {
        super(phase);
        this.validationMode = validationMode;
    }


    /**
     * Intercepts SOAP responses for outbound messages and validates AS4 receipts.
     *
     * <p><b>Processing Flow:</b>
     * <ol>
     *   <li>Extract SOAPMessage from CXF message context</li>
     *   <li>Check if message is an AS4 receipt (has eb:Receipt element)</li>
     *   <li>If receipt: perform schema validation + PEPPOL conformance validation</li>
     *   <li>If validation fails: handle according to {@link #validationMode}</li>
     *   <li>If not a receipt or validation disabled: pass through with no effect</li>
     * </ol>
     *
     * @param message the CXF SOAP message being processed
     * @throws Fault if validation fails in STRICT mode
     */
    @Override
    public void handleMessage(SoapMessage message) throws Fault {

        SOAPMessage response = message.getContent(SOAPMessage.class);

        if (response == null) {
            log.warn("SOAPMessage content is null, skipping receipt validation");
            return;
        }

        // Only validate if it's a receipt message, otherwise skip validation (other non-receipt signals responses)
        if (isReceiptMessage(response)) {
            try {

                validateReceiptSchema(response);

                validatePeppolConfProfileRules(response);

            } catch (OxalisAs4Exception e) {
                handleValidationFailure(e);
            }
        }

    }

    // validates the receipt against the core AS4 schema, including WS-Security and ebms v3, ebbp singals schemas, but excluding PEPPOL-specific schema rules
    private void validateReceiptSchema(SOAPMessage response) throws OxalisAs4Exception {

        try {
            Node messagingNode = getMessagingNode(response);
            Validator validator = SchemaValidatorFactory.getReceiptValidator();

            DefaultValidationErrorHandler errorHandler = SchemaValidatorFactory.newDefaultErrorHandler();
            validator.setErrorHandler(errorHandler);

            validator.validate(new DOMSource(messagingNode));

            if (errorHandler.hasErrors()) {
                throw new OxalisAs4Exception("Receipt schema validation failed: " + errorHandler.getErrorsAsString(), AS4ErrorCode.EBMS_0302);
            }
            log.debug("Receipt schema validated successfully.");
        } catch (IOException | SAXException | OxalisAs4TransmissionException e) {
            throw new OxalisAs4Exception("Error occurred during schema validation: " + e.getMessage(), e, AS4ErrorCode.EBMS_0302);
        }
    }

    // validates the receipt against PEPPOL-specific conformance rules
    private void validatePeppolConfProfileRules(SOAPMessage response) throws OxalisAs4Exception {
        Node receiptNode = getReceiptNode(response);
        PeppolAs4ReceiptValidator.validateConformance(receiptNode);
    }

    // private helpers
    private Node getMessagingNode(SOAPMessage response) throws OxalisAs4TransmissionException {
        try {
            NodeList messagingNodeList = response.getSOAPHeader().getElementsByTagNameNS("*", "Messaging");

            if (messagingNodeList.getLength() != 1) {
                throw new OxalisAs4TransmissionException("Header contains zero or multiple eb:Messaging elements, should only contain one");
            }

            return messagingNodeList.item(0);
        } catch (SOAPException e) {
            throw new OxalisAs4TransmissionException("Could not access response header", e);
        }
    }


    private Node getReceiptNode(SOAPMessage response) throws OxalisAs4Exception {
        try {
            NodeList receiptNodeList = response.getSOAPHeader().getElementsByTagNameNS(Constants.EBMS_NAMESPACE, "Receipt");


            if (receiptNodeList.getLength() != 1) {
                throw new OxalisAs4Exception("Signal message contains multiple Receipt nodes", AS4ErrorCode.EBMS_0302);
            }

            return receiptNodeList.item(0);
        } catch (SOAPException e) {
            throw new OxalisAs4Exception("Could not extract eb:Receipt node from response message", e);
        }
    }

    private boolean isReceiptMessage(SOAPMessage response) {
        try {
            NodeList receiptNodeList = response.getSOAPHeader().getElementsByTagNameNS(Constants.EBMS_NAMESPACE, "Receipt");
            return receiptNodeList.getLength() > 0;
        } catch (SOAPException e) {
            log.warn("Could not access response header to determine if it's a receipt message, assuming it's not a receipt message.");
            return false;
        }
    }

    /**
     * Handles validation failures according to the configured {@link ValidationMode}.
     *
     * <p><b>Failure handling by Mode:</b>
     * <ul>
     *   <li><b>LOGGING:</b> Logs warning with error details, allows processing to continue</li>
     *   <li><b>STRICT:</b> Logs error and throws CXF Fault, terminating the exchange</li>
     *   <li><b>NONE:</b> Should not reach here, but logs debug a message if it does</li>
     * </ul>
     *
     * <p>Debug logging includes full exception stack trace in LOGGING mode for troubleshooting
     * without disrupting production traffic.
     *
     * @param e the validation exception that occurred
     * @throws Fault if in STRICT mode or unknown mode
     */
    private void handleValidationFailure(OxalisAs4Exception e) {
        switch (validationMode) {
            case LOGGING: // default mode
                log.warn("Receipt validation failed (mode: LOGGING - continuing): \n\t{}", e.getMessage());
                if (log.isDebugEnabled()) {
                    log.debug("Validation failure details:", e);
                }
                break;
            case STRICT:
                log.error("Receipt validation failed (mode: STRICT - aborting): \n\t{}", e.getMessage());
                throw new Fault(e);
            case NONE:
                // Should not reach here since validation is skipped, but for the sake of completeness it's here
                log.debug("Validation skipped (mode: NONE)");
                break;
            default:
                log.warn("Unknown validation mode {} - treating as STRICT", validationMode);
                throw new Fault(e);
        }
    }
}

