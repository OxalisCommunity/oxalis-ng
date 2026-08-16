package network.oxalis.ng.as4.mls.certificate;

import com.google.inject.Inject;
import network.oxalis.ng.as4.mls.exception.InvalidCertificateException;
import network.oxalis.ng.commons.security.CertificateUtils;

import java.security.cert.X509Certificate;
import java.util.Objects;

public final class CertificateSpIdExtractor {

    private final SeatIdParser seatIdParser;

    @Inject
    public CertificateSpIdExtractor(SeatIdParser seatIdParser) {
        this.seatIdParser = Objects.requireNonNull(seatIdParser, "seatIdParser");
    }

    public String extractMainId(X509Certificate certificate) {
        Objects.requireNonNull(certificate, "certificate");

        String commonName;
        try {
            commonName = CertificateUtils.extractCommonName(certificate);
        } catch (RuntimeException e) {
            throw new InvalidCertificateException("Could not extract Subject CN from sender certificate: " + e.getMessage());
        }
        return seatIdParser.parseMainId(commonName);
    }
}
