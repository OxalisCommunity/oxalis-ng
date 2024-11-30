package network.oxalis.as4.common;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.api.lang.OxalisLoadingException;
import network.oxalis.vefa.peppol.mode.Mode;
import org.apache.wss4j.common.crypto.Merlin;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.Optional;

@Slf4j
@Singleton
public class MerlinProvider {

    @Inject
    private Mode mode;

    @Inject
    @Named("conf")
    private Path confFolder;

    @Inject
    private KeyStore keyStore;

    @Inject(optional = true)
    @Named("truststore-ap")
    private KeyStore trustStoreAp;

    private KeyStore cachedTrustStore;

    public Merlin getMerlin() {
        Merlin merlin = new Merlin();
        merlin.setCryptoProvider(BouncyCastleProvider.PROVIDER_NAME);
        merlin.setKeyStore(keyStore);
        merlin.setTrustStore(getCachedTrustStore());
        return merlin;
    }

    private KeyStore getCachedTrustStore() {
        if (cachedTrustStore == null) {
            cachedTrustStore = getTrustStore();
        }

        return cachedTrustStore;
    }

    private KeyStore getTrustStore() {
        Optional<KeyStore> trustStoreExtension = loadTrustStoreApFromConf(mode, confFolder);

        if (trustStoreAp != null) {
            trustStoreExtension.ifPresent(p ->
                    extendKeyStore(trustStoreAp, p)
            );

            return trustStoreAp;
        }

        return trustStoreExtension
                .orElseThrow(() -> new OxalisLoadingException("Expected a truststore. Please specify the property security.truststore.ap"));
    }

    private void extendKeyStore(KeyStore trustStoreAp, KeyStore trustStoreExtension) {
        try {
            Enumeration<String> aliases = trustStoreExtension.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                if (!trustStoreAp.containsAlias(alias)) {
                    log.info("Adding {} to truststore", alias);
                    trustStoreAp.setCertificateEntry(alias, trustStoreExtension.getCertificate(alias));
                }
            }
        } catch (KeyStoreException e) {
            throw new OxalisLoadingException("Something went wrong during extension of key store.", e);
        }
    }

    private Optional<KeyStore> loadTrustStoreApFromConf(Mode mode, Path confFolder) {
        String truststoreAp = mode.getString("security.truststore.ap");

        if (truststoreAp == null) {
            return Optional.empty();
        }

        Path path = confFolder.resolve(truststoreAp);

        try {
            KeyStore keystore = KeyStore.getInstance("JKS");
            if (!path.toFile().exists()) return Optional.empty();

            log.info("Loading TRUSTSTORE: {}", path);

            try (InputStream inputStream = Files.newInputStream(path)) {
                keystore.load(inputStream, mode.getString("security.truststore.password").toCharArray());
            }
            return Optional.of(keystore);
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            throw new OxalisLoadingException("Something went wrong during handling of key store.", e);
        } catch (IOException e) {
            throw new OxalisLoadingException(String.format("Error during reading of '%s'.", path), e);
        }
    }
}