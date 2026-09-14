package network.oxalis.ng.commons.identifier;

import network.oxalis.vefa.peppol.common.lang.PeppolParsingException;
import network.oxalis.vefa.peppol.common.model.ParticipantIdentifier;
import network.oxalis.vefa.peppol.common.model.Scheme;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

public class ParticipantIdentifierValidatorTest {

    private static final Scheme DEFAULT_SCHEME = Scheme.of("iso6523-actorid-upis");

    private static ParticipantIdentifier id(String value) {
        return ParticipantIdentifier.of(value, DEFAULT_SCHEME);
    }

    @AfterMethod
    public void resetIcdValidationMode() {
        ParticipantIdentifierValidator.setIcdValidationMode(IcdValidationMode.WARN);
    }

    @Test
    public void validStandardIdentifier() throws PeppolParsingException {
        // 0192 = NO:ORG, a recognised Peppol ICD
        ParticipantIdentifierValidator.validate("receiver", id("0192:987654321"));
        assertTrue(ParticipantIdentifierValidator.isValid(id("0192:987654321")));
    }

    @Test
    public void validMaxLengthIdentifier() {
        // 4-digit ICD + colon + 130 chars = 135 total (maximum allowed per PFUOI v4.4)
        var orgId = "a".repeat(130);
        assertTrue(ParticipantIdentifierValidator.isValid(id("0192:" + orgId)));
    }

    @Test
    public void validMinLengthIdentifier() {
        // 4-digit ICD + colon + 1 char (minimum valid organisation id)
        assertTrue(ParticipantIdentifierValidator.isValid(id("0192:x")));
    }

    @Test
    public void validLongIdentifierWithinNewLimits() {
        // 50 chars for org-id — exceeds the old 28/50 limit but valid under PFUOI v4.4
        var orgId = "a".repeat(50);
        assertTrue(ParticipantIdentifierValidator.isValid(id("0192:" + orgId)));
    }

    @Test
    public void nullIdentifierIsValid() throws PeppolParsingException {
        assertTrue(ParticipantIdentifierValidator.isValid(null));
        ParticipantIdentifierValidator.validate("sender", null);
    }

    @Test
    public void identifierTooLong() {
        // 4-digit ICD + colon + 131 chars = 136 total (exceeds max of 135)
        var orgId = "a".repeat(131);
        assertFalse(ParticipantIdentifierValidator.isValid(id("0192:" + orgId)));
        assertThrows(PeppolParsingException.class,
                () -> ParticipantIdentifierValidator.validate("receiver", id("0192:" + orgId)));
    }

    @Test
    public void unknownIcdIsAcceptedByDefault() {
        // ICD 9999 is not part of the bundled Peppol code list, but the code list is a snapshot
        // that can lag behind newly onboarded jurisdictions — the default WARN mode accepts it.
        assertEquals(ParticipantIdentifierValidator.getIcdValidationMode(), IcdValidationMode.WARN);
        assertTrue(ParticipantIdentifierValidator.isValid(id("9999:987654321")));
    }

    @Test
    public void unknownIcdIsRejectedInStrictMode() {
        ParticipantIdentifierValidator.setIcdValidationMode(IcdValidationMode.STRICT);
        assertFalse(ParticipantIdentifierValidator.isValid(id("9999:987654321")));

        var ex = expectThrows(PeppolParsingException.class,
                () -> ParticipantIdentifierValidator.validate("receiver", id("9999:987654321")));
        assertTrue(ex.getMessage().contains("receiver"));
        assertTrue(ex.getMessage().contains("9999:987654321"));
    }

    @Test
    public void knownIcdIsAcceptedInStrictMode() throws PeppolParsingException {
        ParticipantIdentifierValidator.setIcdValidationMode(IcdValidationMode.STRICT);
        ParticipantIdentifierValidator.validate("receiver", id("0192:987654321"));
    }

    @Test
    public void unknownIcdIsAcceptedInNoneMode() {
        ParticipantIdentifierValidator.setIcdValidationMode(IcdValidationMode.NONE);
        assertTrue(ParticipantIdentifierValidator.isValid(id("9999:987654321")));
    }

    @Test
    public void structuralChecksApplyInEveryMode() {
        // Format and length come from the Peppol policy itself, not the code list snapshot,
        // so they stay fatal even when the code list check is disabled.
        for (IcdValidationMode mode : IcdValidationMode.values()) {
            ParticipantIdentifierValidator.setIcdValidationMode(mode);
            assertFalse(ParticipantIdentifierValidator.isValid(id("abc:987654321")), "mode " + mode);
            assertFalse(ParticipantIdentifierValidator.isValid(id("0192:")), "mode " + mode);
            assertFalse(ParticipantIdentifierValidator.isValid(id("0192:" + "a".repeat(131))), "mode " + mode);
        }
    }

    @Test
    public void identifierMissingIcdPrefix() {
        assertFalse(ParticipantIdentifierValidator.isValid(id("abc:987654321")));
    }

    @Test
    public void identifierMissingColon() {
        assertFalse(ParticipantIdentifierValidator.isValid(id("0192987654321")));
    }

    @Test
    public void identifierWithEmptyOrgId() {
        assertFalse(ParticipantIdentifierValidator.isValid(id("0192:")));
    }

    @Test
    public void identifierWithThreeDigitIcd() {
        assertFalse(ParticipantIdentifierValidator.isValid(id("019:987654321")));
    }

    @Test
    public void identifierWithFiveDigitIcd() {
        assertFalse(ParticipantIdentifierValidator.isValid(id("01920:987654321")));
    }

    @Test
    public void invalidIdentifierMessageContainsRoleAndValue() {
        var ex = expectThrows(PeppolParsingException.class,
                () -> ParticipantIdentifierValidator.validate("receiver", id("abc:bad-id")));
        assertTrue(ex.getMessage().contains("receiver"));
        assertTrue(ex.getMessage().contains("abc:bad-id"));
    }

    @Test
    public void modeParsingIsCaseInsensitive() {
        assertEquals(IcdValidationMode.of("strict"), IcdValidationMode.STRICT);
        assertEquals(IcdValidationMode.of(" Warn "), IcdValidationMode.WARN);
        assertEquals(IcdValidationMode.of("NONE"), IcdValidationMode.NONE);
    }

    @Test
    public void unknownModeFailsFast() {
        assertThrows(IllegalArgumentException.class, () -> IcdValidationMode.of("lenient"));
    }
}
