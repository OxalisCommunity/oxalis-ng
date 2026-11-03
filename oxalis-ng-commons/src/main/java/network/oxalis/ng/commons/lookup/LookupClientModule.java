package network.oxalis.ng.commons.lookup;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import network.oxalis.ng.commons.guice.OxalisModule;
import network.oxalis.ng.commons.mode.OxalisCertificateValidator;
import network.oxalis.vefa.peppol.common.lang.PeppolLoadingException;
import network.oxalis.vefa.peppol.lookup.LookupClient;
import network.oxalis.vefa.peppol.lookup.LookupClientBuilder;
import network.oxalis.vefa.peppol.lookup.api.MetadataFetcher;
import network.oxalis.vefa.peppol.mode.Mode;

/**
 * Provides a single {@link LookupClient} shared by inbound and outbound processing, so that SMP lookups reuse the
 * Oxalis HTTP client and the cached {@link OxalisCertificateValidator}.
 */
public class LookupClientModule extends OxalisModule {

    @Override
    protected void configure() {
        bind(MetadataFetcher.class)
                .to(OxalisApacheFetcher.class);
    }

    @Provides
    @Singleton
    protected LookupClient providesLookupClient(Mode mode, OxalisCertificateValidator certificateValidator,
                                                MetadataFetcher fetcher)
            throws PeppolLoadingException {
        return LookupClientBuilder.forMode(mode)
                .fetcher(fetcher)
                .certificateValidator(certificateValidator)
                .build();
    }
}
