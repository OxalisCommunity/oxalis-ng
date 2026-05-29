package network.oxalis.ng.commons.identifier;

import network.oxalis.vefa.peppol.common.model.ParticipantIdentifier;
import network.oxalis.vefa.peppol.common.model.Scheme;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ParticipantIdentifierValidatorTest {

    private static final Scheme DEFAULT_SCHEME = Scheme.of("iso6523-actorid-upis");

    @Test
    public void validStandardIdentifier() {
        var id = ParticipantIdentifier.of("0192:987654321", DEFAULT_SCHEME);
        assertTrue(ParticipantIdentifierValidator.isValid(id));
        assertTrue(ParticipantIdentifierValidator.validateAndWarn("receiver", id));
    }

    @Test
    public void validMaxLengthIdentifier() {
        // 4-digit ICD + colon + 130 chars = 135 total (maximum allowed per PFUOI v4.4)
        var orgId = "a".repeat(130);
        var id = ParticipantIdentifier.of("0192:" + orgId, DEFAULT_SCHEME);
        assertTrue(ParticipantIdentifierValidator.isValid(id));
    }

    @Test
    public void validMinLengthIdentifier() {
        // 4-digit ICD + colon + 1 char = 6 total (minimum valid)
        var id = ParticipantIdentifier.of("0192:X", DEFAULT_SCHEME);
        assertTrue(ParticipantIdentifierValidator.isValid(id));
    }

    @Test
    public void validOldMaxLengthStillValid() {
        // Old max was 28 chars for org-id — should still be valid under new limits
        var id = ParticipantIdentifier.of("0192:1234567890123456789012345678", DEFAULT_SCHEME);
        assertTrue(ParticipantIdentifierValidator.isValid(id));
    }

    @Test
    public void validLongIdentifierWithinNewLimits() {
        // 50 chars for org-id — exceeds old limit but valid under PFUOI v4.4
        var orgId = "a".repeat(50);
        var id = ParticipantIdentifier.of("0192:" + orgId, DEFAULT_SCHEME);
        assertTrue(ParticipantIdentifierValidator.isValid(id));
    }

    @Test
    public void nullIdentifierIsValid() {
        assertTrue(ParticipantIdentifierValidator.isValid(null));
        assertTrue(ParticipantIdentifierValidator.validateAndWarn("sender", null));
    }

    @Test
    public void identifierTooLong() {
        // 4-digit ICD + colon + 131 chars = 136 total (exceeds max of 135)
        var orgId = "a".repeat(131);
        var id = ParticipantIdentifier.of("0192:" + orgId, DEFAULT_SCHEME);
        assertFalse(ParticipantIdentifierValidator.isValid(id));
        assertFalse(ParticipantIdentifierValidator.validateAndWarn("receiver", id));
    }

    @Test
    public void identifierMissingIcdPrefix() {
        var id = ParticipantIdentifier.of("ABC:987654321", DEFAULT_SCHEME);
        assertFalse(ParticipantIdentifierValidator.isValid(id));
    }

    @Test
    public void identifierMissingColon() {
        var id = ParticipantIdentifier.of("0192987654321", DEFAULT_SCHEME);
        assertFalse(ParticipantIdentifierValidator.isValid(id));
    }

    @Test
    public void identifierWithEmptyOrgId() {
        // 4-digit ICD + colon + nothing = invalid
        var id = ParticipantIdentifier.of("0192:", DEFAULT_SCHEME);
        assertFalse(ParticipantIdentifierValidator.isValid(id));
    }

    @Test
    public void identifierWithThreeDigitIcd() {
        var id = ParticipantIdentifier.of("019:987654321", DEFAULT_SCHEME);
        assertFalse(ParticipantIdentifierValidator.isValid(id));
    }

    @Test
    public void identifierWithFiveDigitIcd() {
        var id = ParticipantIdentifier.of("01920:987654321", DEFAULT_SCHEME);
        assertFalse(ParticipantIdentifierValidator.isValid(id));
    }

    @Test
    public void errorMessageContainsRole() {
        var msg = ParticipantIdentifierValidator.errorMessage("receiver", "bad-id");
        assertTrue(msg.contains("receiver"));
        assertTrue(msg.contains("bad-id"));
        assertTrue(msg.contains("Peppol"));
    }
}

