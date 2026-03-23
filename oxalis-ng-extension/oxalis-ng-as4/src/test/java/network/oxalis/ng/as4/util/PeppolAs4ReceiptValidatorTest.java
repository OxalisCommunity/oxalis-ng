package network.oxalis.ng.as4.util;

import network.oxalis.ng.as4.lang.OxalisAs4Exception;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.testng.Assert.*;

public class PeppolAs4ReceiptValidatorTest {

    private Node validReceiptNode;
    private Node invalidReceiptNode;

    @BeforeClass
    public void setUp() throws Exception {
        // Load receipts from test resources
        validReceiptNode = extractReceiptNode("/ebms/nrr-signal-message.xml");
        invalidReceiptNode = extractReceiptNode("/ebms/nrr-signal-message-invalid-conformance.xml");
    }

    // Positive test case - valid receipt should pass without exceptions
    @Test
    public void validateConformance_validReceipt_passesValidation() throws OxalisAs4Exception {
        // Act & Assert - should not throw
        PeppolAs4ReceiptValidator.validateConformance(validReceiptNode);
    }

    @Test
    public void validateConformance_nullReceiptNode_throwsWithCorrectErrorCode() {
        // Act
        try {
            PeppolAs4ReceiptValidator.validateConformance(null);
            fail("Should have thrown OxalisAs4Exception");
        } catch (OxalisAs4Exception e) {
            // Assert
            assertEquals(e.getErrorCode(), AS4ErrorCode.EBMS_0301, "Should throw with EBMS_0301 error code for missing receipt");
            assertTrue(e.getMessage().contains("Receipt not found"), "Exception message should indicate missing receipt");
        }
    }

    // oxalis-as4 #135
    // invalid receipt with both ds:Reference and ebbpsig:MessagePartIdentifier should throw exception with the correct error code
    @Test
    public void validateConformance_invalidMessagePartNRInformationNode_throwsErrorWithCode() {
        // Act
        try {
            PeppolAs4ReceiptValidator.validateConformance(invalidReceiptNode);
            fail("Should have thrown OxalisAs4Exception");
        } catch (OxalisAs4Exception e) {
            // Assert
            assertEquals(e.getErrorCode(), AS4ErrorCode.EBMS_0302, "Should throw with EBMS_0302 error code for missing NonRepudiationInformation");
            assertTrue(e.getMessage().contains("ds:Reference and ebbpsig:MessagePartIdentifier elements are not allowed together"), "Exception message should indicate the specific conformance issue");
        }
    }

    @Test
    public void validateConformance_noNonRepudiationInfo_throwsOxalisAs4Exception() throws Exception {
        // Arrange
        String xml = "<eb:Receipt xmlns:eb=\"http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/\">"
                + "</eb:Receipt>";
        Node receiptNode = parseXmlString(xml).getDocumentElement();

        // Act
        try {
            PeppolAs4ReceiptValidator.validateConformance(receiptNode);
            fail("Should have thrown OxalisAs4Exception");
        } catch (OxalisAs4Exception e) {
            // Assert
            assertEquals(e.getErrorCode(), AS4ErrorCode.EBMS_0302);
            assertTrue(e.getMessage().contains("NonRepudiationInformation element not found"), "Error message should mention missing NonRepudiationInformation");
        }
    }


    @Test
    public void validateConformance_multipleNonRepudiationInfo_throwsOxalisAs4Exception() throws Exception {
        // Arrange
        String xml = "<eb:Receipt xmlns:eb=\"http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/\""
                + " xmlns:ebbp=\"http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0\">"
                + "  <ebbp:NonRepudiationInformation>"
                + "    <ebbp:MessagePartNRInformation>"
                + "      <ds:Reference xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" URI=\"#test1\">"
                + "        <ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\"/>"
                + "        <ds:DigestValue>6Kvu0nc3EzKKbwJs7XGZfCnQlt971OVi1cm6T6HyBTs=</ds:DigestValue>"
                + "      </ds:Reference>"
                + "    </ebbp:MessagePartNRInformation>"
                + "  </ebbp:NonRepudiationInformation>"
                + "  <ebbp:NonRepudiationInformation>"
                + "    <ebbp:MessagePartNRInformation>"
                + "      <ds:Reference xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" URI=\"#test2\">"
                + "        <ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\"/>"
                + "        <ds:DigestValue>wCtCO88xutufHEOdOHmD3746QjkclPHGGfMkPfPECCI=</ds:DigestValue>"
                + "      </ds:Reference>"
                + "    </ebbp:MessagePartNRInformation>"
                + "  </ebbp:NonRepudiationInformation>"
                + "</eb:Receipt>";
        Node receiptNode = parseXmlString(xml).getDocumentElement();

        // Act
        try {
            PeppolAs4ReceiptValidator.validateConformance(receiptNode);
            fail("Should have thrown OxalisAs4Exception");
        } catch (OxalisAs4Exception e) {
            // Assert
            assertEquals(e.getErrorCode(), AS4ErrorCode.EBMS_0302);
            assertTrue(e.getMessage().contains("Multiple NonRepudiationInformation elements found"), "Error message should mention multiple NonRepudiationInformation");
        }
    }

    @Test
    public void validateConformance_noDsigReferences_throwsOxalisAs4Exception() throws Exception {
        // Arrange
        String xml = "<eb:Receipt xmlns:eb=\"http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/\""
                + " xmlns:ebbp=\"http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0\">"
                + "  <ebbp:NonRepudiationInformation>"
                + "    <ebbp:MessagePartNRInformation>"
                + "    </ebbp:MessagePartNRInformation>"
                + "  </ebbp:NonRepudiationInformation>"
                + "</eb:Receipt>";
        Node receiptNode = parseXmlString(xml).getDocumentElement();

        // Act
        try {
            PeppolAs4ReceiptValidator.validateConformance(receiptNode);
            fail("Should have thrown OxalisAs4Exception");
        } catch (OxalisAs4Exception e) {
            // Assert
            assertEquals(e.getErrorCode(), AS4ErrorCode.EBMS_0302);
            assertTrue(e.getMessage().contains("Digest references are not found"), "Error message should mention missing digest references");
        }
    }


    // helpers
    private Document parseXml(InputStream is) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(is);
    }

    private Document parseXmlString(String xml) throws Exception {
        return parseXml(new ByteArrayInputStream(xml.getBytes()));
    }

    private Node extractReceiptNode(String testResource) throws Exception {

        try (InputStream is = getClass().getResourceAsStream(testResource)) {
            assertNotNull(is, "Test resource " + testResource + " should exist");
            Document doc = parseXml(is);

            NodeList receiptList = doc.getElementsByTagNameNS(
                    "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/",
                    "Receipt"
            );

            if (receiptList.getLength() > 0) {
                return receiptList.item(0);
            }

            return doc.getDocumentElement();
        }


    }


}