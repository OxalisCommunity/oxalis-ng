package network.oxalis.ng.as4.mls.certificate;

import network.oxalis.ng.as4.mls.exception.InvalidCertificateException;
import org.testng.annotations.Test;

import javax.security.auth.x500.X500Principal;
import java.security.cert.X509Certificate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class CertificateSpIdExtractorTest {

    private final CertificateSpIdExtractor extractor = new CertificateSpIdExtractor(new SeatIdParser());

    @Test
    public void extractsMainIdFromCertificateSubjectCn() {
        X509Certificate certificate = certificateWithSubjectCn("POP000723");

        assertEquals(extractor.extractMainId(certificate), "000723");
    }

    @Test(expectedExceptions = InvalidCertificateException.class)
    public void unexpectedSubjectCnThrows() {
        X509Certificate certificate = certificateWithSubjectCn("Not A Seat Id");

        extractor.extractMainId(certificate);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void nullCertificateThrows() {
        extractor.extractMainId(null);
    }

    private static X509Certificate certificateWithSubjectCn(String commonName) {
        X509Certificate certificate = mock(X509Certificate.class);
        when(certificate.getSubjectX500Principal()).thenReturn(new X500Principal("CN=" + commonName));
        return certificate;
    }
}
