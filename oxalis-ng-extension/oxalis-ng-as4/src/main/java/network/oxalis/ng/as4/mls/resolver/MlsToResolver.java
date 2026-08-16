package network.oxalis.ng.as4.mls.resolver;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.ng.as4.mls.certificate.CertificateSpIdExtractor;
import network.oxalis.ng.as4.mls.config.MlsResolutionPolicy;
import network.oxalis.ng.as4.mls.exception.InvalidCertificateException;
import network.oxalis.ng.as4.mls.smp.MlsToSmpValidator;
import network.oxalis.ng.as4.mls.validation.MlsToValidator;
import network.oxalis.vefa.peppol.common.model.Header;
import network.oxalis.vefa.peppol.common.model.MlsToIdentifier;

import java.security.cert.X509Certificate;
import java.util.Objects;

@Slf4j
public final class MlsToResolver {

    private static final String SCHEME_PREFIX = "0242:";

    private final MlsToValidator mlsToValidator;
    private final CertificateSpIdExtractor certificateSpIdExtractor;
    private final MlsToSmpValidator mlsToSmpValidator;
    private final MlsResolutionPolicy policy;

    @Inject
    public MlsToResolver(MlsToValidator mlsToValidator, CertificateSpIdExtractor certificateSpIdExtractor,
                          MlsToSmpValidator mlsToSmpValidator, MlsResolutionPolicy policy) {
        this.mlsToValidator = Objects.requireNonNull(mlsToValidator, "mlsToValidator");
        this.certificateSpIdExtractor = Objects.requireNonNull(certificateSpIdExtractor, "certificateSpIdExtractor");
        this.mlsToSmpValidator = Objects.requireNonNull(mlsToSmpValidator, "mlsToSmpValidator");
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    public Header resolve(Header header, X509Certificate senderCertificate) {
        Objects.requireNonNull(header, "header");
        Objects.requireNonNull(senderCertificate, "senderCertificate");

        MlsToIdentifier existing = header.getMlsToIdentifier();
        if (existing != null && isAcceptable(existing, senderCertificate)) {
            return header;
        }

        return header.mlsToIdentifier(deriveFromCertificate(senderCertificate));
    }

    private boolean isAcceptable(MlsToIdentifier candidate, X509Certificate senderCertificate) {
        if (!mlsToValidator.validate(candidate).isValid()) {
            return false;
        }

        String mainId;
        try {
            mainId = certificateSpIdExtractor.extractMainId(senderCertificate);
        } catch (InvalidCertificateException e) {
            log.warn("Could not derive SPIS Main ID from sender certificate while correlating MLS_TO: {}", e.getMessage());
            return false;
        }

        if (!correlatesToMainId(candidate, mainId)) {
            log.warn("Supplied MLS_TO does not correlate to the sender's SPIS Main ID, ignoring it");
            return false;
        }

        if (!policy.isSmpValidationEnabled()) {
            return true;
        }

        return isRegisteredForMlsSafely(candidate);
    }

    private boolean isRegisteredForMlsSafely(MlsToIdentifier candidate) {
        try {
            return mlsToSmpValidator.isRegisteredForMls(candidate);
        } catch (RuntimeException e) {
            log.warn("MLS_TO SMP validation failed unexpectedly, treating as not confirmed: {}", e.getMessage(), e);
            return false;
        }
    }

    private boolean correlatesToMainId(MlsToIdentifier candidate, String mainId) {
        String value = candidate.getIdentifier();
        String spid = value.substring(SCHEME_PREFIX.length());
        String candidateMainId = spid.split("-", 2)[0];
        return candidateMainId.equalsIgnoreCase(mainId);
    }

    private MlsToIdentifier deriveFromCertificate(X509Certificate senderCertificate) {
        try {
            String mainId = certificateSpIdExtractor.extractMainId(senderCertificate);
            return MlsToIdentifier.of(SCHEME_PREFIX + mainId);
        } catch (InvalidCertificateException e) {
            log.warn("Unable to derive MLS_TO fallback from sender certificate: {}", e.getMessage());
            return null;
        }
    }
}
