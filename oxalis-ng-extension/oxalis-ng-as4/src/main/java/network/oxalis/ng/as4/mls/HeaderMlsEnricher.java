package network.oxalis.ng.as4.mls;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.ng.as4.mls.resolver.MlsToResolver;
import network.oxalis.ng.as4.mls.resolver.MlsTypeResolver;
import network.oxalis.vefa.peppol.common.model.Header;

import java.security.cert.X509Certificate;
import java.util.Objects;

@Slf4j
public final class HeaderMlsEnricher {
    private final MlsToResolver mlsToResolver;
    private final MlsTypeResolver mlsTypeResolver;

    @Inject
    public HeaderMlsEnricher(MlsToResolver mlsToResolver, MlsTypeResolver mlsTypeResolver) {
        this.mlsToResolver = Objects.requireNonNull(mlsToResolver);
        this.mlsTypeResolver = Objects.requireNonNull(mlsTypeResolver);
    }

    public Header enrich(Header header, X509Certificate senderCertificate) {
        Objects.requireNonNull(header);
        Objects.requireNonNull(senderCertificate);

        try {
            Header enriched = mlsToResolver.resolve(header, senderCertificate);
            enriched = mlsTypeResolver.resolve(enriched);
            return enriched;
        } catch (RuntimeException e) {
            log.error("MLS enrichment failed unexpectedly, leaving header unenriched so that inbound processing "
                    + "is not affected: {}", e.getMessage(), e);
            return header;
        }
    }
}