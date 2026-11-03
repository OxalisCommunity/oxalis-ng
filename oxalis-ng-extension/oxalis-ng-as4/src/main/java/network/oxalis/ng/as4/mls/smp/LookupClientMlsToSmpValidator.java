package network.oxalis.ng.as4.mls.smp;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.vefa.peppol.common.lang.EndpointNotFoundException;
import network.oxalis.vefa.peppol.common.model.DocumentTypeIdentifier;
import network.oxalis.vefa.peppol.common.model.MlsToIdentifier;
import network.oxalis.vefa.peppol.common.model.ParticipantIdentifier;
import network.oxalis.vefa.peppol.common.model.ProcessIdentifier;
import network.oxalis.vefa.peppol.common.model.Scheme;
import network.oxalis.vefa.peppol.common.model.TransportProfile;
import network.oxalis.vefa.peppol.lookup.LookupClient;
import network.oxalis.vefa.peppol.lookup.api.LookupException;
import network.oxalis.vefa.peppol.security.lang.PeppolSecurityException;

import java.util.Objects;

@Slf4j
public final class LookupClientMlsToSmpValidator implements MlsToSmpValidator {

    private static final DocumentTypeIdentifier MLS_DOCUMENT_TYPE = DocumentTypeIdentifier.of(
            "urn:oasis:names:specification:ubl:schema:xsd:ApplicationResponse-2::ApplicationResponse##"
                    + "urn:peppol:edec:mls:1.0::2.1",
            Scheme.of("busdox-docid-qns"));

    private static final ProcessIdentifier MLS_PROCESS = ProcessIdentifier.of(
            "urn:peppol:edec:mls",
            Scheme.of("cenbii-procid-ubl"));

    private final LookupClient lookupClient;

    @Inject
    public LookupClientMlsToSmpValidator(LookupClient lookupClient) {
        this.lookupClient = Objects.requireNonNull(lookupClient, "lookupClient");
    }

    @Override
    public boolean isRegisteredForMls(MlsToIdentifier mlsToIdentifier) {
        Objects.requireNonNull(mlsToIdentifier, "mlsToIdentifier");

        try {
            ParticipantIdentifier participant = ParticipantIdentifier.of(mlsToIdentifier.getIdentifier());
            lookupClient.getEndpoint(participant, MLS_DOCUMENT_TYPE, MLS_PROCESS, TransportProfile.PEPPOL_AS4_2_0);
            return true;
        } catch (EndpointNotFoundException e) {
            log.warn("MLS_TO participant '{}' is not registered in SMP for the MLS document type, ignoring it", mlsToIdentifier);
            return false;
        } catch (LookupException | PeppolSecurityException e) {
            log.warn("Could not confirm SMP registration for MLS_TO '{}', ignoring it: {}", mlsToIdentifier, e.getMessage());
            return false;
        }
    }
}
