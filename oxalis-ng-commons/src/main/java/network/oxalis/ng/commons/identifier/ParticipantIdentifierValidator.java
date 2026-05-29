/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package network.oxalis.ng.commons.identifier;

import network.oxalis.vefa.peppol.common.model.ParticipantIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * Utility for validating Peppol participant identifiers against the expected format.
 * <p>
 * A valid Peppol participant identifier value has the form {@code NNNN:orgid} where {@code NNNN} is a 4-digit
 * ICD code and {@code orgid} is 1 to 130 characters, giving a maximum total length of 135 characters.
 * <p>
 * This aligns with the Peppol Policy for Use of Identifiers (PFUOI) v4.4 which increased the maximum
 * participant identifier value length from 50 to 135 (4 + ":" + 130) characters.
 *
 * @since 1.2.3
 */
public final class ParticipantIdentifierValidator {

    private static final Logger log = LoggerFactory.getLogger(ParticipantIdentifierValidator.class);

    /**
     * Maximum length of the organization identifier part (after the ICD and colon),
     * as defined by PFUOI v4.4.
     */
    private static final int MAX_ORG_ID_LENGTH = 130;

    /**
     * Peppol participant identifier pattern: 4-digit ICD code, colon, then 1 to 130 characters.
     * Aligns with PFUOI v4.4 and {@code PeppolIdentifierHelper.MAX_PARTICIPANT_VALUE_LENGTH} (= 135).
     */
    private static final Pattern PARTICIPANT_ID_PATTERN = Pattern.compile("^\\d{4}:.{1," + MAX_ORG_ID_LENGTH + "}$");

    /**
     * Maximum total length of a Peppol participant identifier value: 4 (ICD) + 1 (:) + 130 (org-id) = 135.
     */
    private static final int MAX_PARTICIPANT_VALUE_LENGTH = 4 + 1 + MAX_ORG_ID_LENGTH;

    private ParticipantIdentifierValidator() {
        // utility class
    }

    /**
     * Checks whether the given participant identifier conforms to the ISO 6523 format.
     *
     * @param participantId the participant identifier to validate, may be {@code null}
     * @return {@code true} if valid or {@code null}, {@code false} otherwise
     */
    public static boolean isValid(ParticipantIdentifier participantId) {
        if (participantId == null) {
            return true;
        }
        var identifier = participantId.getIdentifier();
        return identifier == null || PARTICIPANT_ID_PATTERN.matcher(identifier).matches();
    }

    /**
     * Validates a participant identifier and logs a warning if it does not conform to ISO 6523.
     *
     * @param role          a label for the participant role (e.g. "sender", "receiver") used in log messages
     * @param participantId the participant identifier to validate, may be {@code null}
     * @return {@code true} if valid or {@code null}, {@code false} if invalid (a warning is logged)
     */
    public static boolean validateAndWarn(String role, ParticipantIdentifier participantId) {
        if (isValid(participantId)) {
            return true;
        }
        var identifier = participantId.getIdentifier();
        log.warn("Invalid {} participant identifier '{}' (length={}) — "
                        + "expected format is 'NNNN:orgid' with max {} characters total",
                role, identifier, identifier.length(), MAX_PARTICIPANT_VALUE_LENGTH);
        return false;
    }

    /**
     * Returns a human-readable error message for an invalid participant identifier.
     *
     * @param role       a label for the participant role (e.g. "sender", "receiver")
     * @param identifier the raw identifier string
     * @return formatted error message
     */
    public static String errorMessage(String role, String identifier) {
        return String.format(
                "Invalid %s participant identifier '%s' — does not conform to expected Peppol format "
                        + "(expected 4-digit ICD, colon, 1-%d char org id, max %d characters total)",
                role, identifier, MAX_ORG_ID_LENGTH, MAX_PARTICIPANT_VALUE_LENGTH);
    }
}

