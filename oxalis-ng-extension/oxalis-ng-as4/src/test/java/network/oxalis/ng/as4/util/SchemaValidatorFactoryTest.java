package network.oxalis.ng.as4.util;

import network.oxalis.ng.api.lang.OxalisResourceException;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import java.io.IOException;
import java.io.InputStream;

import static org.testng.Assert.*;

public class SchemaValidatorFactoryTest {


    // Schema caching tests

    @Test
    public void getSchema_sameContextCalledTwice_returnsSameCachedInstance() {

        // Arrange
        SchemaValidatorFactory.ValidationContext context = SchemaValidatorFactory.ValidationContext.SIGNAL_MESSAGE;

        // Act
        var schema1 = SchemaValidatorFactory.getSchema(context);
        var schema2 = SchemaValidatorFactory.getSchema(context);

        // Assert
        assertNotNull(schema1);
        assertNotNull(schema2);

        assertSame(schema1, schema2);

    }

    @Test
    public void getSchema_differentContexts_returnsDifferentInstances() {

        // Arrange
        SchemaValidatorFactory.ValidationContext context1 = SchemaValidatorFactory.ValidationContext.SIGNAL_MESSAGE;
        SchemaValidatorFactory.ValidationContext context2 = SchemaValidatorFactory.ValidationContext.USER_MESSAGE;

        // Act
        var schema1 = SchemaValidatorFactory.getSchema(context1);
        var schema2 = SchemaValidatorFactory.getSchema(context2);

        // Assert
        assertNotNull(schema1);
        assertNotNull(schema2);

        assertNotSame(schema1, schema2);

    }

    @Test
    public void getSchema_customSchemaPathsSameKey_returnsSameCachedInstance() {
        // Arrange
        String[] schemaPaths = {
                "ebxml/ebms-header-3_0-200704.xsd",
                "w3/xmldsig-core-schema.xsd",
                "xmlsoap/envelope.xsd"
        };

        // Act
        Schema schema1 = SchemaValidatorFactory.getSchema(schemaPaths);
        Schema schema2 = SchemaValidatorFactory.getSchema(schemaPaths);

        // Assert
        assertNotNull(schema1, "First schema instance should not be null");
        assertNotNull(schema2, "Second schema instance should not be null");
        assertSame(schema2, schema1, "Same schema paths should return the same cached instance");
    }

    @Test
    public void getSchema_customSchemaPathsDifferentKeys_returnsDifferentInstances() {
        // Arrange
        String[] schemaPaths1 = {
                "ebxml/ebms-header-3_0-200704.xsd",
                "w3/xmldsig-core-schema.xsd"
        };

        String[] schemaPaths2 = {
                "ebxml/ebms-header-3_0-200704.xsd",
                "w3/xmldsig-core-schema.xsd",
                "ebxml/ebbp-signals-2.0.xsd"  // Different - has additional schema
        };

        // Act
        Schema schema1 = SchemaValidatorFactory.getSchema(schemaPaths1);
        Schema schema2 = SchemaValidatorFactory.getSchema(schemaPaths2);

        // Assert
        assertNotNull(schema1, "First schema instance should not be null");
        assertNotNull(schema2, "Second schema instance should not be null");
        assertNotSame(schema2, schema1, "Different schema paths should return different instances");
    }


    // negative tests for invalid schema paths

    @Test(expectedExceptions = OxalisResourceException.class)
    public void getSchema_nonExistentSchemaPath_throwsResourceException() {
        // Arrange
        String[] schemaPaths = {
                "nonexistent/path/schema.xsd"
        };

        // Act
        SchemaValidatorFactory.getSchema(schemaPaths);
    }

    @Test(expectedExceptions = OxalisResourceException.class)
    public void getSchema_wrongSchemaPath_throwsResourceException() {
        // Arrange
        String[] schemaPaths = {
                "../../../soap-envelope.xsd",
        };

        // Act
        SchemaValidatorFactory.getSchema(schemaPaths);
    }


    // validator tests
    @Test
    public void getValidator_validContext_returnsValidator() {
        // Arrange
        SchemaValidatorFactory.ValidationContext context = SchemaValidatorFactory.ValidationContext.SIGNAL_MESSAGE;

        // Act
        var validator = SchemaValidatorFactory.getValidator(context);

        // Assert
        assertNotNull(validator, "Validator should not be null for valid context");
    }

    @Test
    public void getValidator_multipleCallsSameContext_returnsDifferentValidatorInstances() {
        // Arrange
        SchemaValidatorFactory.ValidationContext context = SchemaValidatorFactory.ValidationContext.USER_MESSAGE;

        // Act
        var validator1 = SchemaValidatorFactory.getValidator(context);
        var validator2 = SchemaValidatorFactory.getValidator(context);

        // Assert
        assertNotNull(validator1, "First validator instance should not be null");
        assertNotNull(validator2, "Second validator instance should not be null");
        assertNotSame(validator1, validator2, "Multiple calls should return different validator instances");
    }

    // positive test for user message validation
    @Test
    public void validateUserMessage_successfulValidation() throws IOException, SAXException {
        // Arrange
        Validator validator = SchemaValidatorFactory.getValidator(SchemaValidatorFactory.ValidationContext.USER_MESSAGE);
        SchemaValidatorFactory.DefaultValidationErrorHandler errorHandler = SchemaValidatorFactory.newDefaultErrorHandler();
        validator.setErrorHandler(errorHandler);

        // Act
        InputStream is = getClass().getResourceAsStream("/ebms/user-message.xml");

        try (is) {
            assertNotNull(is, "Test file user-message.xml should exist in test resources");

            StreamSource source = new StreamSource(is);
            source.setSystemId("ebms/user-message.xml");

            validator.validate(source);

            // Assert
            assertFalse(errorHandler.hasErrors(),
                    "User message should be valid. Errors: " + errorHandler.getErrorsAsString());

        }
    }

    // negative test for an invalid user message
    @Test
    public void validateUserMessage_invalidMessage_hasValidationErrors() throws IOException, SAXException {

        Validator validator = SchemaValidatorFactory.getValidator(SchemaValidatorFactory.ValidationContext.USER_MESSAGE);
        SchemaValidatorFactory.DefaultValidationErrorHandler errorHandler = SchemaValidatorFactory.newDefaultErrorHandler();
        validator.setErrorHandler(errorHandler);

        // Act
        InputStream is = getClass().getResourceAsStream("/ebms/user-message-invalid.xml");

        try (is) {
            assertNotNull(is, "Test file user-message.xml should exist in test resources");
            StreamSource source = new StreamSource(is);
            source.setSystemId("ebms/user-message-invalid.xml");

            validator.validate(source);

            // Assert
            assertTrue(errorHandler.hasErrors(), "Invalid user message should have validation errors");
            String errors = errorHandler.getErrorsAsString();

            System.out.println("Validation errors for invalid user message:\n" + errors);
        }
    }

    // positive test for the receipt message
    @Test
    public void validateReceiptMessage_successfulValidation() throws IOException, SAXException {
        // Arrange
        Validator validator = SchemaValidatorFactory.getValidator(SchemaValidatorFactory.ValidationContext.NRR_RECEIPT);
        SchemaValidatorFactory.DefaultValidationErrorHandler errorHandler = SchemaValidatorFactory.newDefaultErrorHandler();
        validator.setErrorHandler(errorHandler);

        // Act
        InputStream is = getClass().getResourceAsStream("/ebms/nrr-signal-message.xml");

        try (is) {
            assertNotNull(is, "Test file nrr-signal-message.xml should exist in test resources");

            StreamSource source = new StreamSource(is);
            source.setSystemId("ebms/nrr-signal-message.xml");

            validator.validate(source);

            // Assert
            assertFalse(errorHandler.hasErrors(),
                    "Signal message should be valid. Errors: " + errorHandler.getErrorsAsString());

        }
    }


    // negative test for an invalid signal message
    @Test
    public void validateReceiptMessage_invalidMessage_hasValidationErrors() throws IOException, SAXException {
        Validator validator = SchemaValidatorFactory.getValidator(SchemaValidatorFactory.ValidationContext.NRR_RECEIPT);
        SchemaValidatorFactory.DefaultValidationErrorHandler errorHandler = SchemaValidatorFactory.newDefaultErrorHandler();
        validator.setErrorHandler(errorHandler);

        // Act
        InputStream is = getClass().getResourceAsStream("/ebms/nrr-signal-message-invalid-schema.xml");

        try (is) {
            assertNotNull(is, "Test file nrr-signal-message-invalid-schema.xml should exist in test resources");

            StreamSource source = new StreamSource(is);
            source.setSystemId("ebms/nrr-signal-message-invalid-schema.xml");

            validator.validate(source);

            // Assert
            assertTrue(errorHandler.hasErrors(), "Invalid signal message should have validation errors");
            String errors = errorHandler.getErrorsAsString();

            System.out.println("Validation errors for invalid signal message:\n" + errors);
        }
    }

}