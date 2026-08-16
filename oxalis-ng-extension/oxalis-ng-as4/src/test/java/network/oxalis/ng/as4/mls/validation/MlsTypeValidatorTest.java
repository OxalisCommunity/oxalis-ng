package network.oxalis.ng.as4.mls.validation;

import network.oxalis.vefa.peppol.common.model.MlsTypeIdentifier;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class MlsTypeValidatorTest {

    private final MlsTypeValidator validator = new MlsTypeValidator();

    @Test
    public void nullIsInvalid() {
        assertFalse(validator.validate(null).isValid());
    }

    @Test
    public void failureOnlyIsValid() {
        assertTrue(validator.validate(MlsTypeIdentifier.of("FAILURE_ONLY")).isValid());
    }

    @Test
    public void alwaysSendIsValid() {
        assertTrue(validator.validate(MlsTypeIdentifier.of("ALWAYS_SEND")).isValid());
    }

    @Test
    public void wrongCaseIsInvalid() {
        assertFalse(validator.validate(MlsTypeIdentifier.of("always_send")).isValid());
    }

    @Test
    public void unknownValueIsInvalid() {
        assertFalse(validator.validate(MlsTypeIdentifier.of("SOMETHING_ELSE")).isValid());
    }
}
