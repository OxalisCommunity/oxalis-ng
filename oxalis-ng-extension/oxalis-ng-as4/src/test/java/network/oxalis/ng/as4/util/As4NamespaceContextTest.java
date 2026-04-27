package network.oxalis.ng.as4.util;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.xml.XMLConstants;

import static org.testng.Assert.*;

public class As4NamespaceContextTest {

    private final As4NamespaceContext context = As4NamespaceContext.getInstance();

    @DataProvider(name = "prefixToUriDataProvider")
    public Object[][] prefixToUriDataProvider() {
        return new Object[][]{
                {"env", "http://www.w3.org/2003/05/soap-envelope"},
                {"eb", "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/"},
                {"ebbp", "http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0"},
                {"ds", "http://www.w3.org/2000/09/xmldsig#"},
                {"sbdh", "http://www.unece.org/cefact/namespaces/StandardBusinessDocumentHeader"},
                {"unknown", XMLConstants.NULL_NS_URI}
        };
    }


    @DataProvider(name = "uriToPrefixDataProvider")
    public Object[][] uriToPrefixDataProvider() {
        return new Object[][]{
                {"http://www.w3.org/2003/05/soap-envelope", "env"},
                {"http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/", "eb"},
                {"http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0", "ebbp"},
                {"http://www.w3.org/2000/09/xmldsig#", "ds"},
                {"http://www.unece.org/cefact/namespaces/StandardBusinessDocumentHeader", "sbdh"}
        };
    }


    @Test(dataProvider = "prefixToUriDataProvider")
    public void getNamespaceURI_validPrefix_returnsNamespaceUri(String prefix, String expectedUri) {
        String actualUri = context.getNamespaceURI(prefix);
        assertEquals(actualUri, expectedUri, "Namespace URI for prefix '" + prefix + "' did not match expected value.");
    }

    @Test(dataProvider = "uriToPrefixDataProvider")
    public void getPrefix_validUri_returnsPrefix(String namespaceURI, String expectedPrefix) {
        String actualPrefix = context.getPrefix(namespaceURI);
        assertEquals(actualPrefix, expectedPrefix, "Prefix for namespace URI '" + namespaceURI + "' did not match expected value.");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void getPrefix_unknownNamespaceUri_throwsIllegalArgumentException() {
        String unknownNamespaceUri = "http://unknown.namespace/uri";
        context.getPrefix(unknownNamespaceUri);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void getPrefix_nullNamespaceUri_throwsIllegalArgumentException() {
        context.getPrefix(null);
    }
}