package network.oxalis.ng.as4.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.ng.api.lang.OxalisResourceException;
import org.apache.cxf.common.xmlschema.LSInputImpl;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for creating XML Schema validators for AS4 message validation.
 *
 * <p>This factory provides thread-safe, cached Schema and Validator instances for validating
 * AS4/ebMS messages against the relevant XSD schemas. It can handle schemas for pre-defined validations contexts or custom sets of schemas.:
 * <ul>
 *   <li>SOAP 1.2 envelope structure (SOAP 1.2 is mandatory for AS4)</li>
 *   <li>ebMS v3.0 messaging headers (Oasis AS4 protocol)</li>
 *   <li>EBBP signals for receipts and errors</li>
 *   <li>XML Digital Signatures</li>
 * </ul>
 *
 * @see ValidationContext for pre-defined validation scenarios
 * @see DefaultValidationErrorHandler for error collection and reporting
 */
@Slf4j
public class SchemaValidatorFactory {

    // XSD Schema resource paths
    private static final String SOAP_ENVELOPE = "w3/soap-envelope.xsd";
    private static final String EBMS_V3 = "ebxml/ebms-header-3_0-200704.xsd";
    private static final String EBBP_SIGNAL = "ebxml/ebbp-signals-2.0.xsd";
    private static final String XML_DSIG = "w3/xmldsig-core-schema.xsd";

    /**
     * Cache for pre-compiled schemas by validation context.
     * Thread-safe and prevents redundant schema compilation.
     */
    private static final Map<ValidationContext, Schema> schemaCache = new ConcurrentHashMap<>();
    private static final Map<String, Schema> customSchemaCache = new ConcurrentHashMap<>();


    /**
     * Pre-defined validation contexts for common AS4 message types.
     * Each context defines which XSD schemas must be compiled together for a message type.
     *
     * <p><b>Available Contexts:</b>
     * <ul>
     *   <li><b>SIGNAL_MESSAGE:</b> For AS4 receipts and errors (SOAP + ebMS + XMLDsig)</li>
     *   <li><b>USER_MESSAGE:</b> For AS4 user messages carrying business documents (SOAP + ebMS + XMLDsig)</li>
     *   <li><b>NRR_RECEIPT:</b> For non-repudiation receipts specifically (ebMS + EBBP + XMLDsig, no SOAP envelope)</li>
     * </ul>
     */
    @Getter
    public enum ValidationContext {

        SIGNAL_MESSAGE(
                SOAP_ENVELOPE,
                EBMS_V3,
                XML_DSIG
        ),  // for EBBP signals like PullRequest, ReceiptNotification (ebMS header + SOAP envelope)

        USER_MESSAGE(
                SOAP_ENVELOPE,
                EBMS_V3,
                XML_DSIG
        ), // for user messages (ebMS header + SOAP envelope)

        NRR_RECEIPT(
                EBMS_V3,
                EBBP_SIGNAL,
                XML_DSIG
        ); // for NonRepudiationInformation in receipts (ebMS header + EBBP signals, no SOAP envelope)

        private final String[] schemaPaths;

        ValidationContext(String... schemaPaths) {
            this.schemaPaths = schemaPaths;
        }

    }

    // ==================== Public API ====================

    /**
     * Returns a compiled, cached Schema for the specified validation context.
     *
     * <p>Schemas are compiled once and cached for performance. This method is thread-safe.
     *
     * @param context the validation context defining which schemas to combine
     * @return compiled Schema instance (cached)
     * @throws OxalisResourceException on schema compilation failure
     */
    public static Schema getSchema(ValidationContext context) {
        return schemaCache.computeIfAbsent(context, SchemaValidatorFactory::getCompiledSchema);
    }

    /**
     * Returns a compiled, cached Schema for custom schema paths.
     *
     * <p>Use this when you need to validate against a custom combination of schemas
     * not covered by the pre-defined {@link ValidationContext} options.
     *
     * @param schemaPaths classpath-relative paths to XSD files
     * @return compiled Schema instance (cached with path concatenation as key)
     * @throws OxalisResourceException on schema compilation failure
     */
    public static Schema getSchema(String... schemaPaths) {
        String key = String.join("|", schemaPaths);
        return customSchemaCache.computeIfAbsent(key, k -> getCompiledSchema(schemaPaths));
    }

    /**
     * Creates a new Validator for the specified validation context.
     *
     * <p>Note: Validators are NOT thread-safe and should not be shared between threads.
     * Create a new validator for each validation operation.
     *
     * @param context the validation context defining which schemas to be validated against
     * @return new Validator instance
     * @throws OxalisResourceException on schema compilation failure
     */
    public static Validator getValidator(ValidationContext context) {
        Schema schema = getSchema(context);
        return schema.newValidator();
    }

    /**
     * Creates a new Validator for custom schema paths.
     *
     * @param schemaPaths classpath-relative paths to XSD files
     * @return new Validator instance
     * @throws OxalisResourceException on schema compilation failure
     */
    public static Validator getValidator(String... schemaPaths) {
        Schema schema = getSchema(schemaPaths);
        return schema.newValidator();
    }

    public static Validator getReceiptValidator() {
        return getValidator(ValidationContext.NRR_RECEIPT);
    }

    public static DefaultValidationErrorHandler newDefaultErrorHandler() {
        return new DefaultValidationErrorHandler();
    }


    // private helpers

    private static Schema getCompiledSchema(ValidationContext validationContext) {
        return getCompiledSchema(validationContext.getSchemaPaths());
    }

    /**
     * Compiles multiple XSD schemas into a single Schema object.
     *
     * <p>This method:
     * <ol>
     *   <li>Loads all schema files from the classpath into memory</li>
     *   <li>Configures the custom resource resolver for XSD imports</li>
     *   <li>Compiles all schemas together into a single Schema instance</li>
     * </ol>
     *
     * <p>Synchronized to ensure thread-safe schema compilation (SchemaFactory is not thread-safe
     * during configuration changes).
     *
     * @param schemaPaths classpath-relative paths to XSD files
     * @return compiled Schema instance
     * @throws OxalisResourceException if schema loading or compilation fails
     */
    private static synchronized Schema getCompiledSchema(String... schemaPaths) {
        try {

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schemaFactory.setResourceResolver(new ClasspathSchemaResolver());

            Source[] sources = loadSchemaSources(schemaPaths);

            return schemaFactory.newSchema(sources);

        } catch (SAXException e) {
            log.error("Error compiling schema: {}", e.getMessage(), e);
            throw new OxalisResourceException("Error compiling schema", e);
        }
    }


    /**
     * Loads schema files from the classpath into an array of StreamSource objects.
     *
     * <p>Schemas are fully read into memory to avoid resource leaks and to ensure
     * the streams remain available during validation. This is acceptable since
     * XSD files are typically small (< 100KB each).
     *
     * @param schemaPaths classpath-relative paths to XSD files
     * @return array of StreamSource objects
     * @throws OxalisResourceException on schema loading failure (file not found or read error / all or nothing)
     */

    private static Source[] loadSchemaSources(String... schemaPaths) {
        Source[] sources = new Source[schemaPaths.length];

        for (int i = 0; i < schemaPaths.length; i++) {
            String path = schemaPaths[i];

            try (InputStream is = SchemaValidatorFactory.class.getClassLoader().getResourceAsStream(path)) {
                if (is == null) {
                    throw new OxalisResourceException("Schema resource not found: " + path);
                }

                // the schemas are not big and read typically once, so this is fine
                byte[] schemaBytes = is.readAllBytes();

                sources[i] = new StreamSource(new ByteArrayInputStream(schemaBytes), path);

            } catch (IOException e) {
                throw new OxalisResourceException("Failed to read schema resource: " + path, e);
            }
        }

        return sources;
    }

    /**
     * Custom LSResourceResolver for resolving XSD imports and includes from classpath.
     *
     * <p>This resolver handles:
     * <ul>
     *   <li>Relative path resolution (../ and ./)</li>
     *   <li>Common XSD location prefixes (/ebxml/, /w3/, /xmlsoap/)</li>
     *   <li>Fallback to the root classpath if no prefix matches</li>
     * </ul>
     *
     * <p>This is necessary because XSD schemas often reference other schemas using
     * relative or absolute paths that need to be resolved against the classpath.
     */
    private static class ClasspathSchemaResolver implements LSResourceResolver {
        @Override
        public LSInput resolveResource(String type, String namespaceURI,
                                       String publicId, String systemId, String baseURI) {
            InputStream stream = tryLoadSchema(systemId, baseURI);
            return stream != null ? new LSInputImpl(publicId, systemId, stream) : null;
        }

        /**
         * Attempts to load a schema from the classpath using multiple resolution strategies.
         *
         * @param systemId the schema identifier (filename or path)
         * @param baseURI  the URI of the schema containing the reference (for relative path resolution)
         * @return InputStream to the schema, or null if not found
         */
        private InputStream tryLoadSchema(String systemId, String baseURI) {
            if (systemId == null) return null;

            // Strategy 1: Resolve relative paths (../ or ./) against the base URI
            if (baseURI != null && (systemId.startsWith("../") || systemId.startsWith("./"))) {
                String resolved = resolveRelativePath(baseURI, systemId);
                InputStream stream = getClass().getResourceAsStream(resolved);
                if (stream != null) return stream;
            }

            // Strategy 2: Try common classpath prefixes where XSD files are typically located
            for (String prefix : new String[]{"", "/ebxml/", "/w3/", "/xmlsoap/"}) {
                InputStream stream = getClass().getResourceAsStream(prefix + systemId);
                if (stream != null) return stream;
            }

            return null;
        }

        private String resolveRelativePath(String baseURI, String systemId) {
            try {
                URI base = new URI(baseURI);
                URI resolved = base.resolve(systemId);

                return resolved.getPath();
            } catch (Exception e) {
                return systemId;
            }
        }
    }


    /**
     * SAX ErrorHandler that collects validation errors and warnings for reporting .
     *
     * <p>This handler accumulates all errors and warnings during validation, allowing
     * the caller to inspect them after validation completes. It distinguishes between:
     * <ul>
     *   <li><b>Warnings:</b> Non-fatal issues that don't prevent processing</li>
     *   <li><b>Errors:</b> Schema violations that make the document invalid</li>
     *   <li><b>Fatal Errors:</b> Severe problems that prevent further parsing</li>
     * </ul>
     *
     */
    @Getter
    public static class DefaultValidationErrorHandler implements ErrorHandler {

        private final List<SAXParseException> errors = new ArrayList<>();
        private final List<SAXParseException> warnings = new ArrayList<>();


        @Override
        public void warning(SAXParseException exception) {
            warnings.add(exception);
        }

        @Override
        public void error(SAXParseException exception) {
            errors.add(exception);
        }

        @Override
        public void fatalError(SAXParseException exception) {
            errors.add(exception);
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public String getErrorsAsString() {
            StringBuilder sb = new StringBuilder();
            for (SAXParseException e : errors) {
                sb.append(String.format("Line %d, Column %d: %s%n", e.getLineNumber(), e.getColumnNumber(), e.getMessage()));
            }
            return sb.toString();
        }

        public String getWarningsAsString() {
            StringBuilder sb = new StringBuilder();
            for (SAXParseException e : warnings) {
                sb.append(String.format("Line %d, Column %d: %s%n", e.getLineNumber(), e.getColumnNumber(), e.getMessage()));
            }
            return sb.toString();
        }

        public String getValidationSummary() {
            StringBuilder sb = new StringBuilder();
            if (!warnings.isEmpty()) {
                sb.append("Warnings:\n").append(getWarningsAsString());
            }
            if (!errors.isEmpty()) {
                sb.append("Errors:\n").append(getErrorsAsString());
            }
            return sb.toString();
        }
    }

}
