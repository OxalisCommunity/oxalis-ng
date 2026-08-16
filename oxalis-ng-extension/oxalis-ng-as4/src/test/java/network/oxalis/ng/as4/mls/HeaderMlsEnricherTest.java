package network.oxalis.ng.as4.mls;

import network.oxalis.ng.as4.mls.certificate.CertificateSpIdExtractor;
import network.oxalis.ng.as4.mls.certificate.SeatIdParser;
import network.oxalis.ng.as4.mls.config.MlsResolutionPolicy;
import network.oxalis.ng.as4.mls.resolver.MlsToResolver;
import network.oxalis.ng.as4.mls.resolver.MlsTypeResolver;
import network.oxalis.ng.as4.mls.validation.MlsToValidator;
import network.oxalis.ng.as4.mls.validation.MlsTypeValidator;
import network.oxalis.vefa.peppol.common.model.Header;
import network.oxalis.vefa.peppol.common.model.MlsToIdentifier;
import network.oxalis.vefa.peppol.common.model.MlsTypeIdentifier;
import org.testng.annotations.Test;

import javax.security.auth.x500.X500Principal;
import java.security.cert.X509Certificate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class HeaderMlsEnricherTest {

    private final HeaderMlsEnricher enricher = new HeaderMlsEnricher(
            new MlsToResolver(new MlsToValidator(),
                    new CertificateSpIdExtractor(new SeatIdParser()),
                    mlsToIdentifier -> true,
                    new MlsResolutionPolicy(MlsTypeIdentifier.of("FAILURE_ONLY"), false)),
            new MlsTypeResolver(new MlsTypeValidator(),
                    new MlsResolutionPolicy(MlsTypeIdentifier.of("FAILURE_ONLY"), false)));

    @Test
    public void resolvesBothMlsToAndMlsTypeWhenAbsent() {
        Header header = Header.newInstance();
        X509Certificate certificate = certificateWithSubjectCn("POP000723");

        Header enriched = enricher.enrich(header, certificate);

        assertEquals(enriched.getMlsToIdentifier(), MlsToIdentifier.of("0242:000723"));
        assertEquals(enriched.getMlsTypeIdentifier(), MlsTypeIdentifier.of("FAILURE_ONLY"));
    }

    @Test
    public void preservesExistingValidValues() {
        Header header = Header.newInstance()
                .mlsToIdentifier(MlsToIdentifier.of("0242:000723"))
                .mlsTypeIdentifier(MlsTypeIdentifier.of("ALWAYS_SEND"));
        X509Certificate certificate = certificateWithSubjectCn("POP000723");

        Header enriched = enricher.enrich(header, certificate);

        assertEquals(enriched.getMlsToIdentifier(), MlsToIdentifier.of("0242:000723"));
        assertEquals(enriched.getMlsTypeIdentifier(), MlsTypeIdentifier.of("ALWAYS_SEND"));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void requiresHeader() {
        enricher.enrich(null, mock(X509Certificate.class));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void requiresCertificate() {
        enricher.enrich(Header.newInstance(), null);
    }

    private static X509Certificate certificateWithSubjectCn(String commonName) {
        X509Certificate certificate = mock(X509Certificate.class);
        when(certificate.getSubjectX500Principal()).thenReturn(new X500Principal("CN=" + commonName));
        return certificate;
    }
}
