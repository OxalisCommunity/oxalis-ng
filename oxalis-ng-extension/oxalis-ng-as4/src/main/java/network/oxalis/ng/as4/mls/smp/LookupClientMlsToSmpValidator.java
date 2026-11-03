package network.oxalis.ng.as4.mls.smp;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.ng.api.settings.Settings;
import network.oxalis.ng.as4.mls.config.MlsConf;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Checks MLS_TO participants against SMP. Definitive answers (registered or not registered) are cached for a
 * configurable time, transient lookup failures are not, so they are retried on the next message.
 */
@Slf4j
@Singleton
public final class LookupClientMlsToSmpValidator implements MlsToSmpValidator {

    private static final DocumentTypeIdentifier MLS_DOCUMENT_TYPE = DocumentTypeIdentifier.of(
            "urn:oasis:names:specification:ubl:schema:xsd:ApplicationResponse-2::ApplicationResponse##"
                    + "urn:peppol:edec:mls:1.0::2.1",
            Scheme.of("busdox-docid-qns"));

    private static final ProcessIdentifier MLS_PROCESS = ProcessIdentifier.of(
            "urn:peppol:edec:mls",
            Scheme.of("cenbii-procid-ubl"));

    private static final long MAXIMUM_CACHE_SIZE = 1000;

    private final LookupClient lookupClient;

    private final LoadingCache<MlsToIdentifier, Boolean> registrations;

    @Inject
    public LookupClientMlsToSmpValidator(LookupClient lookupClient, Settings<MlsConf> settings) {
        this(lookupClient, settings.getInt(MlsConf.SMP_VALIDATION_CACHE_TIME_TO_LIVE));
    }

    LookupClientMlsToSmpValidator(LookupClient lookupClient, long cacheTimeToLiveSeconds) {
        this.lookupClient = Objects.requireNonNull(lookupClient, "lookupClient");
        this.registrations = CacheBuilder.newBuilder()
                .maximumSize(MAXIMUM_CACHE_SIZE)
                .expireAfterWrite(cacheTimeToLiveSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<MlsToIdentifier, Boolean>() {
                    @Override
                    public Boolean load(MlsToIdentifier mlsToIdentifier) throws LookupException, PeppolSecurityException {
                        return lookupRegistration(mlsToIdentifier);
                    }
                });
    }

    @Override
    public boolean isRegisteredForMls(MlsToIdentifier mlsToIdentifier) {
        Objects.requireNonNull(mlsToIdentifier, "mlsToIdentifier");

        try {
            return registrations.get(mlsToIdentifier);
        } catch (ExecutionException e) {
            log.warn("Could not confirm SMP registration for MLS_TO '{}', ignoring it: {}",
                    mlsToIdentifier, e.getCause().getMessage());
            return false;
        }
    }

    private boolean lookupRegistration(MlsToIdentifier mlsToIdentifier) throws LookupException, PeppolSecurityException {
        try {
            ParticipantIdentifier participant = ParticipantIdentifier.of(mlsToIdentifier.getIdentifier());
            lookupClient.getEndpoint(participant, MLS_DOCUMENT_TYPE, MLS_PROCESS, TransportProfile.PEPPOL_AS4_2_0);
            return true;
        } catch (EndpointNotFoundException e) {
            log.warn("MLS_TO participant '{}' is not registered in SMP for the MLS document type, ignoring it", mlsToIdentifier);
            return false;
        }
    }
}
