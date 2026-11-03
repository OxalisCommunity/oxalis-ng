package network.oxalis.ng.commons.mode;

import com.typesafe.config.ConfigFactory;
import network.oxalis.commons.certvalidator.api.CrlFetcher;
import network.oxalis.pkix.ocsp.api.OcspFetcher;
import network.oxalis.vefa.peppol.common.code.Service;
import network.oxalis.vefa.peppol.mode.Mode;
import network.oxalis.vefa.peppol.security.api.CertificateValidator;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Map;

public class ModeModuleCertificateValidatorTest {

    private final OcspFetcher ocspFetcher = (uri, content) -> {
        throw new UnsupportedOperationException();
    };

    private final CrlFetcher crlFetcher = url -> null;

    @Test
    public void certificateValidatorIsInitiatedWithOxalisFetchers() throws Exception {
        Mode mode = Mode.of(ConfigFactory.parseMap(Collections.singletonMap(
                "security.validator.class", RecordingCertificateValidator.class.getName())), Mode.TEST);

        CertificateValidator validator = new ModeModule().getCertificateValidator(mode, ocspFetcher, crlFetcher);

        Assert.assertTrue(validator instanceof RecordingCertificateValidator);
        Map<String, Object> objectStorage = ((RecordingCertificateValidator) validator).objectStorage;
        Assert.assertSame(objectStorage.get(CertificateValidatorObjectStorage.OCSP_FETCHER_KEY), ocspFetcher);
        Assert.assertSame(objectStorage.get(CertificateValidatorObjectStorage.CRL_FETCHER_KEY), crlFetcher);
    }

    @Test
    public void objectStorageIsMutable() {
        Map<String, Object> objectStorage = CertificateValidatorObjectStorage.of(ocspFetcher, crlFetcher);

        objectStorage.put("other", new Object());

        Assert.assertEquals(objectStorage.size(), 3);
    }

    public static class RecordingCertificateValidator implements CertificateValidator {

        private final Map<String, Object> objectStorage;

        public RecordingCertificateValidator(Mode mode, Map<String, Object> objectStorage) {
            this.objectStorage = objectStorage;
        }

        @Override
        public void validate(Service service, X509Certificate certificate) {
            // Not used
        }
    }
}
