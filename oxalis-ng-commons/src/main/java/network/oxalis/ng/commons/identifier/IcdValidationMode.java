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

import java.util.Locale;

/**
 * Controls how {@link ParticipantIdentifierValidator} reacts to an ICD that is not part of the
 * Peppol participant identifier scheme code list bundled with vefa-peppol.
 * <p>
 * The bundled code list is a snapshot: when a new jurisdiction joins the Peppol network its ICD is
 * published by OpenPeppol before a vefa-peppol release (and an oxalis-ng upgrade) can pick it up.
 * The mode lets an access point decide whether such an unknown-but-well-formed ICD should be fatal,
 * a warning, or ignored. Structural checks (length, {@code icd:organizationId} format, 4-digit
 * numeric ICD) are always enforced regardless of mode.
 * <p>
 * Configured through the {@code oxalis.identifier.icd.validation} setting.
 *
 * @since 1.4.0
 */
public enum IcdValidationMode {

    /**
     * An ICD absent from the bundled code list is fatal: the identifier is rejected.
     */
    STRICT,

    /**
     * An ICD absent from the bundled code list is logged as a warning and processing continues.
     * This is the default: it keeps the signal without rejecting participants from jurisdictions
     * newer than the bundled code list.
     */
    WARN,

    /**
     * The code list membership check is skipped entirely.
     */
    NONE;

    /**
     * Parses a configuration value into a mode, case-insensitively.
     *
     * @param value the raw configuration value
     * @return the matching mode
     * @throws IllegalArgumentException if the value does not name a mode, so that a typo in the
     *                                  configuration fails at startup instead of silently changing
     *                                  validation behaviour
     */
    public static IcdValidationMode of(String value) {
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format(
                    "Unknown ICD validation mode '%s', expected one of STRICT, WARN or NONE.", value), e);
        }
    }
}
