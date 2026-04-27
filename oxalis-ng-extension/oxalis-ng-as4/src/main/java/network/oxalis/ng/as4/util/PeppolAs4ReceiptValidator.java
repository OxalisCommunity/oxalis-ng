package network.oxalis.ng.as4.util;

import lombok.extern.slf4j.Slf4j;
import network.oxalis.ng.as4.lang.OxalisAs4Exception;
import network.oxalis.ng.as4.outbound.As4ReceiptValidationInInterceptor;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Validates PEPPOL-specific conformance rules for AS4 receipt messages.
 *
 * <p>This validator enforces requirements beyond standard Oasis AS4 that are mandated by the
 * <b>PEPPOL AS4 Profile</b>, which extends the <b>CEF eDelivery AS4 Profile</b>. These
 * additional rules ensure legal non-repudiation and interoperability across the PEPPOL network.
 *
 * <p><b>Specification References:</b>
 * <ul>
 *   <li><a href="https://docs.peppol.eu/edelivery/as4/specification/">PEPPOL AS4 Profile</a></li>
 *   <li><a href="https://ec.europa.eu/digital-building-blocks/sites/display/DIGITAL/eDelivery+AS4+-+1.14+-+Reliable+Messaging+and+Non-Repudiation+of+Receipt">
 *       CEF eDelivery AS4 Profile - Section 1.14</a></li>
 *   <li><a href="https://docs.oasis-open.org/ebxml-msg/ebms/v3.0/profiles/AS4-profile/v1.0/os/AS4-profile-v1.0-os.html#__RefHeading__26454_1909778835">
 *       OASIS AS4 Profile - Section 5.1.9 (Non-Repudiation)</a></li>
 * </ul>
 *
 * @see As4NamespaceContext for namespace prefix mappings used in XPath expressions
 * @see As4ReceiptValidationInInterceptor for where this validator is invoked
 */

@Slf4j
public final class PeppolAs4ReceiptValidator {

    /**
     * Private constructor to prevent instantiation.
     * This class is a utility class with only static methods, no instantiation is needed or allowed.
     */
    private PeppolAs4ReceiptValidator() {
    }


    /**
     * Validates that an AS4 receipt conforms to PEPPOL-specific requirements.
     *
     * <p>This is the main entry point for PEPPOL receipt validation. It performs a
     * hierarchical series of checks, failing fast on the first violation:
     *
     * <ol>
     *   <li><b>NonRepudiationInformation check:</b> Exactly one element must exist</li>
     *   <li><b>MessagePartNRInformation check:</b> At least one element must exist</li>
     *   <li><b>Reference format check:</b> Only ds:Reference allowed (no MessagePartIdentifier)</li>
     * </ol>
     *
     * <p><b>Error Handling:</b>
     * All validation failures throw {@link OxalisAs4Exception} with error code EBMS_0302
     * (Invalid Receipt), which indicates the receipt structure doesn't conform to
     * the required AS4/PEPPOL profile. On an empty or null receipt, EBMS_0301 (Missing Receipt) is thrown instead.
     *
     * @param receiptNode the eb:Receipt DOM node to validate (must not be null)
     * @throws OxalisAs4Exception if the receipt is null or fails any PEPPOL conformance rule
     */
    public static void validateConformance(Node receiptNode) throws OxalisAs4Exception {
        if (receiptNode == null) {
            throw new OxalisAs4Exception("Receipt not found in the response message", AS4ErrorCode.EBMS_0301);
        }

        // Validates in hierarchical order (fail-fast)
        checkNonRepudiationInformation(receiptNode);
        checkMessagePartNRInformation(receiptNode);
        checkMessagePartReferences(receiptNode);

        log.debug("PEPPOL receipt conformance validated successfully.");
    }


    /**
     * Since PEPPOL AS4 Receipt MUST contain NonRepudiationInformation element on synchronous transmission responses
     * as per PEPPOL AS4 profile > CEF eDelivery Common Profile
     *
     * <p><b>Specification Reference:</b>
     * <a href="https://ec.europa.eu/digital-building-blocks/sites/display/DIGITAL/eDelivery+AS4+-+1.14+-+Reliable+Messaging+and+Non-Repudiation+of+Receipt">
     * CEF eDelivery AS4 - Section 1.14: Reliable Messaging and Non-Repudiation of Receipt</a>
     */
    private static void checkNonRepudiationInformation(Node receiptNode)
            throws OxalisAs4Exception {

        var nonRepudiationInfoElements = getElements(receiptNode, "//ebbp:NonRepudiationInformation");

        if (nonRepudiationInfoElements.getLength() == 0) {
            throw new OxalisAs4Exception("NonRepudiationInformation element not found", AS4ErrorCode.EBMS_0302);
        }

        if (nonRepudiationInfoElements.getLength() > 1) {
            throw new OxalisAs4Exception("Multiple NonRepudiationInformation elements found", AS4ErrorCode.EBMS_0302);
        }

    }


    public static void checkMessagePartNRInformation(Node receiptNode)
            throws OxalisAs4Exception {
        var messagePartNRInfoElements = getElements(receiptNode, "//ebbp:NonRepudiationInformation/ebbp:MessagePartNRInformation");

        if (messagePartNRInfoElements.getLength() == 0) {
            throw new OxalisAs4Exception("MessagePartNRInformation element(s) not found", AS4ErrorCode.EBMS_0302);
        }
    }

    public static void checkMessagePartReferences(Node receiptNode) throws OxalisAs4Exception {
        var messagePartNRInfoElements = getElements(receiptNode, "//ebbp:NonRepudiationInformation/ebbp:MessagePartNRInformation");

        for (int i = 0; i < messagePartNRInfoElements.getLength(); i++) {
            var dsigRefs = getElements(messagePartNRInfoElements.item(i), "ds:Reference");
            var messagePartIdentifiers = getElements(messagePartNRInfoElements.item(i), "ebbp:MessagePartIdentifier");

            boolean hasDsigRefs = dsigRefs.getLength() > 0;
            boolean hasMessagePartIdentifiers = messagePartIdentifiers.getLength() > 0;

            // checks conformance for message part references in AS4 receipts as per
            // https://docs.oasis-open.org/ebxml-msg/ebms/v3.0/profiles/AS4-profile/v1.0/os/AS4-profile-v1.0-os.html#__RefHeading__26454_1909778835 [Section 5.1.9]

            //  explicit checking for #135
            if (hasDsigRefs && hasMessagePartIdentifiers) {
                throw new OxalisAs4Exception("Both ds:Reference and ebbpsig:MessagePartIdentifier elements are not allowed together in Peppol AS4 receipts", AS4ErrorCode.EBMS_0302);
            }

            if (!hasDsigRefs) {
                throw new OxalisAs4Exception("Digest references are not found in the non-repudiation receipt", AS4ErrorCode.EBMS_0302);
            }
        }
    }


    private static void checkElementExists(Node context, String xpathExpression, String errorMessage) throws OxalisAs4Exception {
        NodeList nodes = getElements(context, xpathExpression);

        if (nodes.getLength() == 0) {
            throw new OxalisAs4Exception(errorMessage, AS4ErrorCode.EBMS_0302);
        }
    }

    private static synchronized NodeList getElements(Node context, String xpathExpression) throws OxalisAs4Exception {
        try {

            XPath xPath = XPathFactory.newInstance().newXPath();
            xPath.setNamespaceContext(As4NamespaceContext.getInstance());

            return (NodeList) xPath.evaluate(
                    xpathExpression,
                    context,
                    XPathConstants.NODESET
            );
        } catch (XPathExpressionException e) {
            throw new OxalisAs4Exception("XPath evaluation failed: " + xpathExpression, e);
        }
    }

}