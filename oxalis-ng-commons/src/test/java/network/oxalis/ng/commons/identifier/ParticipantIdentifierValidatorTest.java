package network.oxalis.ng.commons.identifier;

import network.oxalis.vefa.peppol.common.lang.PeppolParsingException;
import network.oxalis.vefa.peppol.common.model.ParticipantIdentifier;
import network.oxalis.vefa.peppol.common.model.Scheme;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

public class ParticipantIdentifierValidatorTest {

    private static final Scheme DEFAULT_SCHEME = Scheme.of("iso6523-actorid-upis");

    private static ParticipantIdentifier id(String value) {
        return ParticipantIdentifier.of(value, DEFAULT_SCHEME);
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
    public void identifierWithUnknownIcd() {
        // ICD 9999 is not part of the Peppol participant identifier scheme code list
        assertFalse(ParticipantIdentifierValidator.isValid(id("9999:987654321")));
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
                () -> ParticipantIdentifierValidator.validate("receiver", id("9999:bad-id")));
        assertTrue(ex.getMessage().contains("receiver"));
        assertTrue(ex.getMessage().contains("9999:bad-id"));
    }
}
