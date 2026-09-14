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

import lombok.extern.slf4j.Slf4j;
import network.oxalis.vefa.peppol.common.lang.PeppolParsingException;
import network.oxalis.vefa.peppol.common.model.Header;
import network.oxalis.vefa.peppol.common.model.ParticipantIdentifier;
import network.oxalis.vefa.peppol.icd.Icds;
import network.oxalis.vefa.peppol.icd.code.PeppolIcd;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Central entry point for validating the Peppol participant identifiers carried by an SBDH header.
 * <p>
 * Validation is intentionally lightweight and does not attempt full ISO 6523 semantic validation
 * (e.g. organisation-number check digits). Two tiers of checks are applied:
 * <ul>
 *     <li><b>Structural checks, always fatal.</b> The value must have the form
 *     {@code icd:organizationId} with a 4-digit numeric ICD and a non-empty organisation identifier,
 *     and must not exceed {@value #MAX_PARTICIPANT_VALUE_LENGTH} characters as defined by the Peppol
 *     Policy for use of Identifiers (PFUOI) v4.4 (4-digit ICD + {@code ':'} + up to 130 characters).
 *     These rules come from the policy itself and can never reject a legitimate identifier.</li>
 *     <li><b>ICD code list membership, mode-controlled.</b> The ICD is looked up in the Peppol
 *     participant identifier scheme code list bundled with vefa-peppol ({@link Icds} over
 *     {@link PeppolIcd}). That list is a snapshot that can lag behind OpenPeppol publications when a
 *     new jurisdiction joins the network, so the reaction to an unknown ICD is governed by the
 *     configured {@link IcdValidationMode} (default {@link IcdValidationMode#WARN}: log and
 *     continue).</li>
 * </ul>
 * <p>
 * A failing identifier is reported by throwing a {@link PeppolParsingException}: callers treat an invalid
 * identifier as fatal and abort processing.
 *
 * @since 1.2.3
 */
@Slf4j
public final class ParticipantIdentifierValidator {

    /**
     * Maximum total length of a Peppol participant identifier value as defined by PFUOI v4.4:
     * 4 (ICD) + 1 ({@code ':'}) + 130 (organisation id) = 135 characters.
     */
    public static final int MAX_PARTICIPANT_VALUE_LENGTH = 135;

    /**
     * The ICD part of a participant identifier is always a 4-digit numeric code (ISO 6523).
     */
    private static final Pattern ICD_PATTERN = Pattern.compile("[0-9]{4}");

    /**
     * vefa-peppol registry of the officially recognised Peppol participant identifier scheme (ICD) codes.
     */
    private static final Icds ICDS = Icds.of(PeppolIcd.values());

    /**
     * Reaction to an ICD absent from the bundled code list. Set once at startup by
     * {@link IdentifierModule} from the {@code oxalis.identifier.icd.validation} setting; the field
     * is static because validation is also reached from objects built outside the injector (e.g.
     * the document sniffer's SBDH representation).
     */
    private static volatile IcdValidationMode icdValidationMode = IcdValidationMode.WARN;

    private ParticipantIdentifierValidator() {
        // utility class
    }

    /**
     * Sets the reaction to an ICD that is not part of the bundled Peppol code list.
     *
     * @param mode the mode to apply from now on
     */
    public static void setIcdValidationMode(IcdValidationMode mode) {
        icdValidationMode = Objects.requireNonNull(mode, "mode");
    }

    /**
     * @return the currently applied reaction to an ICD absent from the bundled code list
     */
    public static IcdValidationMode getIcdValidationMode() {
        return icdValidationMode;
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

        if (!ICD_PATTERN.matcher(identifier.substring(0, separator)).matches()) {
            throw new PeppolParsingException(errorMessage(role, identifier,
                    "the ICD part must be a 4-digit numeric code"));
        }

        if (icdValidationMode == IcdValidationMode.NONE) {
            return;
        }

        try {
            ICDS.parse(participantId);
        } catch (PeppolParsingException e) {
            if (icdValidationMode == IcdValidationMode.STRICT) {
                throw new PeppolParsingException(errorMessage(role, identifier, e.getMessage()), e);
            }
            // WARN: the ICD is well-formed but not in the bundled code list. It may belong to a
            // jurisdiction newer than this build, so keep processing instead of rejecting.
            log.warn("The {} participant identifier '{}' uses an ICD that is not in the bundled Peppol "
                            + "code list ({}). Accepting it; set oxalis.identifier.icd.validation=STRICT to reject.",
                    role, identifier, e.getMessage());
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
