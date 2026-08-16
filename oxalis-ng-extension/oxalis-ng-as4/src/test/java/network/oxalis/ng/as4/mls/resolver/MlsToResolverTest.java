package network.oxalis.ng.as4.mls.resolver;

import network.oxalis.ng.as4.mls.certificate.CertificateSpIdExtractor;
import network.oxalis.ng.as4.mls.certificate.SeatIdParser;
import network.oxalis.ng.as4.mls.config.MlsResolutionPolicy;
import network.oxalis.ng.as4.mls.smp.MlsToSmpValidator;
import network.oxalis.ng.as4.mls.validation.MlsToValidator;
import network.oxalis.vefa.peppol.common.model.Header;
import network.oxalis.vefa.peppol.common.model.MlsToIdentifier;
import org.testng.annotations.Test;

import javax.security.auth.x500.X500Principal;
import java.security.cert.X509Certificate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class MlsToResolverTest {

    private final CertificateSpIdExtractor certificateSpIdExtractor = new CertificateSpIdExtractor(new SeatIdParser());

    @Test
    public void preservesValidCorrelatedMlsTo() {
        MlsToResolver resolver = resolver(false, true);
        Header header = Header.newInstance().mlsToIdentifier(MlsToIdentifier.of("0242:000723"));
        X509Certificate certificate = certificateWithSubjectCn("POP000723");

        Header resolved = resolver.resolve(header, certificate);

        assertEquals(resolved.getMlsToIdentifier(), MlsToIdentifier.of("0242:000723"));
    }

    @Test
    public void derivesFromCertificateWhenAbsent() {
        MlsToResolver resolver = resolver(false, true);
        Header header = Header.newInstance();
        X509Certificate certificate = certificateWithSubjectCn("POP000723");

        Header resolved = resolver.resolve(header, certificate);

        assertEquals(resolved.getMlsToIdentifier(), MlsToIdentifier.of("0242:000723"));
    }

    @Test
    public void derivesFromCertificateWhenSyntacticallyInvalid() {
        MlsToResolver resolver = resolver(false, true);
        Header header = Header.newInstance().mlsToIdentifier(MlsToIdentifier.of("not-a-valid-value"));
        X509Certificate certificate = certificateWithSubjectCn("POP000723");

        Header resolved = resolver.resolve(header, certificate);

        assertEquals(resolved.getMlsToIdentifier(), MlsToIdentifier.of("0242:000723"));
    }

    @Test
    public void derivesFromCertificateWhenMainIdDoesNotCorrelate() {
        MlsToResolver resolver = resolver(false, true);
        Header header = Header.newInstance().mlsToIdentifier(MlsToIdentifier.of("0242:999999"));
        X509Certificate certificate = certificateWithSubjectCn("POP000723");

        Header resolved = resolver.resolve(header, certificate);

        assertEquals(resolved.getMlsToIdentifier(), MlsToIdentifier.of("0242:000723"));
    }

    @Test
    public void derivesFromCertificateWhenSmpValidationEnabledAndFails() {
        MlsToResolver resolver = resolver(true, false);
        Header header = Header.newInstance().mlsToIdentifier(MlsToIdentifier.of("0242:000723"));
        X509Certificate certificate = certificateWithSubjectCn("POP000723");

        Header resolved = resolver.resolve(header, certificate);

        assertEquals(resolved.getMlsToIdentifier(), MlsToIdentifier.of("0242:000723"));
    }

    @Test
    public void derivesFromCertificateWhenSmpValidatorThrowsUnexpectedly() {
        MlsToSmpValidator smpValidator = mock(MlsToSmpValidator.class);
        when(smpValidator.isRegisteredForMls(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new IllegalStateException("simulated unexpected SMP client failure"));
        MlsResolutionPolicy policy = new MlsResolutionPolicy(defaultMlsType(), true);
        MlsToResolver resolver = new MlsToResolver(new MlsToValidator(), certificateSpIdExtractor, smpValidator, policy);

        Header header = Header.newInstance().mlsToIdentifier(MlsToIdentifier.of("0242:000723-MLS"));
        X509Certificate certificate = certificateWithSubjectCn("POP000723");

        Header resolved = resolver.resolve(header, certificate);

        assertEquals(resolved.getMlsToIdentifier(), MlsToIdentifier.of("0242:000723"));
    }

    @Test
    public void doesNotConsultSmpValidatorWhenDisabled() {
        MlsToSmpValidator smpValidator = mock(MlsToSmpValidator.class);
        when(smpValidator.isRegisteredForMls(org.mockito.ArgumentMatchers.any())).thenReturn(false);
        MlsResolutionPolicy policy = new MlsResolutionPolicy(defaultMlsType(), false);
        MlsToResolver resolver = new MlsToResolver(new MlsToValidator(), certificateSpIdExtractor, smpValidator, policy);

        Header header = Header.newInstance().mlsToIdentifier(MlsToIdentifier.of("0242:000723"));
        X509Certificate certificate = certificateWithSubjectCn("POP000723");

        Header resolved = resolver.resolve(header, certificate);

        assertEquals(resolved.getMlsToIdentifier(), MlsToIdentifier.of("0242:000723"));
    }

    @Test
    public void gracefullyDegradesWhenCertificateCannotYieldMainId() {
        MlsToResolver resolver = resolver(false, true);
        Header header = Header.newInstance();
        X509Certificate certificate = certificateWithSubjectCn("Not A Seat Id");

        Header resolved = resolver.resolve(header, certificate);

        assertNull(resolved.getMlsToIdentifier());
    }

    private MlsToResolver resolver(boolean smpValidationEnabled, boolean smpValidationResult) {
        MlsToSmpValidator smpValidator = mock(MlsToSmpValidator.class);
        when(smpValidator.isRegisteredForMls(org.mockito.ArgumentMatchers.any())).thenReturn(smpValidationResult);
        MlsResolutionPolicy policy = new MlsResolutionPolicy(defaultMlsType(), smpValidationEnabled);
        return new MlsToResolver(new MlsToValidator(), certificateSpIdExtractor, smpValidator, policy);
    }

    private static network.oxalis.vefa.peppol.common.model.MlsTypeIdentifier defaultMlsType() {
        return network.oxalis.vefa.peppol.common.model.MlsTypeIdentifier.of("FAILURE_ONLY");
    }

    private static X509Certificate certificateWithSubjectCn(String commonName) {
        X509Certificate certificate = mock(X509Certificate.class);
        when(certificate.getSubjectX500Principal()).thenReturn(new X500Principal("CN=" + commonName));
        return certificate;
    }
}
