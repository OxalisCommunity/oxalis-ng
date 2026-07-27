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

import network.oxalis.vefa.peppol.common.lang.PeppolParsingException;
import network.oxalis.vefa.peppol.common.model.Header;
import network.oxalis.vefa.peppol.common.model.ParticipantIdentifier;
import network.oxalis.vefa.peppol.icd.Icds;
import network.oxalis.vefa.peppol.icd.code.PeppolIcd;

/**
 * Central entry point for validating the Peppol participant identifiers carried by an SBDH header.
 * <p>
 * Validation is intentionally lightweight and does not attempt full ISO 6523 semantic validation
 * (e.g. organisation-number check digits). It verifies that a participant identifier value:
 * <ul>
 *     <li>has the structural form {@code icd:organizationId} with a non-empty organisation identifier;</li>
 *     <li>carries an ICD that is part of the Peppol participant identifier scheme code list, by delegating
 *     to vefa-peppol's {@link Icds} rather than a hand-rolled regular expression;</li>
 *     <li>does not exceed the maximum length of {@value #MAX_PARTICIPANT_VALUE_LENGTH} characters defined by
 *     the Peppol Policy for use of Identifiers (PFUOI) v4.4 (4-digit ICD + {@code ':'} + up to 130 characters).</li>
 * </ul>
 * <p>
 * A failing identifier is reported by throwing a {@link PeppolParsingException}: callers treat an invalid
 * identifier as fatal and abort processing, so validation is deliberately not a "log a warning and continue"
 * operation.
 *
 * @since 1.2.3
 */
public final class ParticipantIdentifierValidator {

    /**
     * Maximum total length of a Peppol participant identifier value as defined by PFUOI v4.4:
     * 4 (ICD) + 1 ({@code ':'}) + 130 (organisation id) = 135 characters.
     */
    public static final int MAX_PARTICIPANT_VALUE_LENGTH = 135;

    /**
     * vefa-peppol registry of the officially recognised Peppol participant identifier scheme (ICD) codes.
     */
    private static final Icds ICDS = Icds.of(PeppolIcd.values());

    private ParticipantIdentifierValidator() {
        // utility class
    }

    /**
     * Validates the sender and receiver participant identifiers of the given header.
     *
     * @param header the parsed SBDH header
     * @throws PeppolParsingException if the sender or receiver participant identifier is invalid
     */
    public static void validate(Header header) throws PeppolParsingException {
        validate(header.getSender(), header.getReceiver());
    }

    /**
     * Validates a sender/receiver pair of participant identifiers.
     *
     * @param sender   the sender participant identifier, may be {@code null}
     * @param receiver the receiver participant identifier, may be {@code null}
     * @throws PeppolParsingException if either participant identifier is invalid
     */
    public static void validate(ParticipantIdentifier sender, ParticipantIdentifier receiver)
            throws PeppolParsingException {
        validate("sender", sender);
        validate("receiver", receiver);
    }

    /**
     * Validates a single participant identifier.
     *
     * @param role          a label for the participant role (e.g. "sender", "receiver") used in error messages
     * @param participantId the participant identifier to validate, may be {@code null}
     * @throws PeppolParsingException if the participant identifier is invalid
     */
    public static void validate(String role, ParticipantIdentifier participantId) throws PeppolParsingException {
        if (participantId == null || participantId.getIdentifier() == null) {
            return;
        }

        String identifier = participantId.getIdentifier();

        if (identifier.length() > MAX_PARTICIPANT_VALUE_LENGTH) {
            throw new PeppolParsingException(errorMessage(role, identifier, String.format(
                    "value length %d exceeds the maximum of %d characters",
                    identifier.length(), MAX_PARTICIPANT_VALUE_LENGTH)));
        }

        int separator = identifier.indexOf(':');
        if (separator < 0 || separator == identifier.length() - 1) {
            throw new PeppolParsingException(errorMessage(role, identifier,
                    "expected format 'icd:organizationId' with a non-empty organisation identifier"));
        }

        try {
            ICDS.parse(participantId);
        } catch (PeppolParsingException e) {
            throw new PeppolParsingException(errorMessage(role, identifier, e.getMessage()), e);
        }
    }

    /**
     * Convenience non-throwing check.
     *
     * @param participantId the participant identifier to validate, may be {@code null}
     * @return {@code true} if the identifier is valid (or {@code null}), {@code false} otherwise
     */
    public static boolean isValid(ParticipantIdentifier participantId) {
        try {
            validate("participant", participantId);
            return true;
        } catch (PeppolParsingException e) {
            return false;
        }
    }

    private static String errorMessage(String role, String identifier, String reason) {
        return String.format(
                "Invalid %s participant identifier '%s': %s", role, identifier, reason);
    }
}
