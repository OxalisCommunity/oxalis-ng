package network.oxalis.ng.as4.mls.validation;

import network.oxalis.vefa.peppol.common.model.MlsToIdentifier;
import network.oxalis.vefa.peppol.common.model.Scheme;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class MlsToValidatorTest {

    private final MlsToValidator validator = new MlsToValidator();

    @Test
    public void nullIsInvalid() {
        assertFalse(validator.validate(null).isValid());
    }

    @Test
    public void mainIdOnlyIsValid() {
        assertTrue(validator.validate(MlsToIdentifier.of("0242:000723")).isValid());
    }

    @Test
    public void mainIdWithUseCaseIdIsValid() {
        assertTrue(validator.validate(MlsToIdentifier.of("0242:987654-MLS")).isValid());
    }

    @Test
    public void mainIdWithUseCaseIdAndSuffixIsValid() {
        assertTrue(validator.validate(MlsToIdentifier.of("0242:987654-MLS.svc01")).isValid());
    }

    @Test
    public void wrongParticipantSchemePrefixIsInvalid() {
        assertFalse(validator.validate(MlsToIdentifier.of("0208:012345678")).isValid());
    }

    @Test
    public void tooFewMainIdDigitsIsInvalid() {
        assertFalse(validator.validate(MlsToIdentifier.of("0242:12345")).isValid());
    }

    @Test
    public void garbageValueIsInvalid() {
        assertFalse(validator.validate(MlsToIdentifier.of("not-an-identifier")).isValid());
    }

    @Test
    public void nonDefaultSchemeIsInvalid() {
        MlsToIdentifier identifier = MlsToIdentifier.of("0242:000723", Scheme.of("some-other-scheme"));
        assertFalse(validator.validate(identifier).isValid());
    }
}
