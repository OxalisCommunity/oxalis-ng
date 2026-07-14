package network.oxalis.ng.as4.util;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import java.util.Iterator;
import java.util.Map;

/**
 * Namespace context for AS4/ebMS/PEPPOL XML processing with bidirectional prefix-URI mappings.
 *
 * <p>This class provides a centralized, immutable mapping of XML namespace prefixes to URIs
 * for all AS4-related specifications. It implements the JAXP {@link NamespaceContext} interface
 * required by XPath expressions, XSLT transformations, and XML parsing operations.
 *
 * <p><b>Supported Namespaces:</b>
 * <ul>
 *   <li><b>env:</b> SOAP 1.2 envelope (mandatory for AS4)</li>
 *   <li><b>eb:</b> ebMS v3.0 messaging headers (core AS4 protocol)</li>
 *   <li><b>ebbp:</b> EBBP signals (receipts and errors)</li>
 *   <li><b>ds:</b> XML Digital Signature (WS-Security)</li>
 *   <li><b>sbdh:</b> Standard Business Document Header (PEPPOL envelope)</li>
 * </ul>
 *
 * <p><b>Additional Notes:</b>
 * <ul>
 *   <li><b>Immutable:</b> Namespace mappings cannot be modified (thread-safe by design)</li>
 *   <li><b>Singleton:</b> Single instance shared across the application (reduces overhead)</li>
 *   <li><b>Thread-safety:</b> This class is designed for thread-safety. The singleton instance and all mappings are immutable.</li>
 *   <li><b>Bidirectional:</b> Supports both prefix→URI and URI→prefix lookups</li>
 *   <li><b>Standard Prefixes:</b> Uses conventional prefixes matching specification documentation</li>
 *   <li><b>Fallback Behavior:</b> Unrecognized prefixes return XMLConstants.NULL_NS_URI, and unrecognized URIs throw IllegalArgumentException</li>
 * </ul>
 *
 * @see javax.xml.namespace.NamespaceContext
 * @see Constants for the actual namespace URI definitions
 */
public final class As4NamespaceContext implements NamespaceContext {

    // prefix to URI mappings
    private static final Map<String, String> PREFIX_TO_URI;

    static {
        PREFIX_TO_URI = Map.ofEntries(
                Map.entry("env", Constants.SOAP12_ENV_NAMESPACE),
                Map.entry("eb", Constants.EBMS_NAMESPACE),
                Map.entry("ebbp", Constants.EBBP_SIGNALS_NAMESPACE),
                Map.entry("ds", Constants.DSIG_NAMESPACE),
                Map.entry("sbdh", Constants.SBDH_NAMESPACE)
        );
    }

    // singleton scope
    private static final As4NamespaceContext INSTANCE = new As4NamespaceContext();

    // private constructor to prevent instantiation
    private As4NamespaceContext() {
    }

    // public accessor for singleton instance
    public static As4NamespaceContext getInstance() {
        return INSTANCE;
    }


    // prefix to namespace URI mapping
    @Override
    public String getNamespaceURI(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("Prefix cannot be null");
        }

        String uri = PREFIX_TO_URI.get(prefix);

        if (uri != null) {
            return uri;
        }

        return XMLConstants.NULL_NS_URI;
    }

    // namespace URI to prefix mapping
    @Override
    public String getPrefix(String namespaceURI) {

        if (namespaceURI == null) {
            throw new IllegalArgumentException("Namespace URI cannot be null");
        }

        return PREFIX_TO_URI.entrySet().stream()
                .filter(e -> e.getValue().equals(namespaceURI))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No prefix found for namespace URI: " + namespaceURI));
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {
        return PREFIX_TO_URI.entrySet().stream()
                .filter(e -> e.getValue().equals(namespaceURI))
                .map(Map.Entry::getKey)
                .iterator();

    }

    // return all mappings
    public Map<String, String> getAllMappings() {
        return Map.copyOf(PREFIX_TO_URI);
    }
}
