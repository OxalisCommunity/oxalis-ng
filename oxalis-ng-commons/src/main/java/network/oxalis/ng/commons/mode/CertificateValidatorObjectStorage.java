package network.oxalis.ng.commons.mode;

import network.oxalis.commons.certvalidator.api.CrlFetcher;
import network.oxalis.pkix.ocsp.api.OcspFetcher;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds the object storage handed to vefa when initiating certificate validators, so that validation rules use the
 * Oxalis configured OCSP and CRL fetchers instead of the library defaults.
 */
final class CertificateValidatorObjectStorage {

    static final String OCSP_FETCHER_KEY = "ocsp_fetcher";

    static final String CRL_FETCHER_KEY = "crlFetcher";

    private CertificateValidatorObjectStorage() {
    }

    /**
     * @return a new mutable map, as rule parsers may add entries to the object storage they receive.
     */
    static Map<String, Object> of(OcspFetcher ocspFetcher, CrlFetcher crlFetcher) {
        Map<String, Object> objectStorage = new HashMap<>();
        objectStorage.put(OCSP_FETCHER_KEY, ocspFetcher);
        objectStorage.put(CRL_FETCHER_KEY, crlFetcher);
        return objectStorage;
    }
}
