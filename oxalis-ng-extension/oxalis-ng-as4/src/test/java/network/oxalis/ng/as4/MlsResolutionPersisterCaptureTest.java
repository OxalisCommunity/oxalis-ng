package network.oxalis.ng.as4;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import network.oxalis.ng.api.inbound.InboundMetadata;
import network.oxalis.ng.api.inbound.InboundService;
import network.oxalis.ng.api.model.TransmissionIdentifier;
import network.oxalis.ng.api.outbound.MessageSender;
import network.oxalis.ng.api.outbound.TransmissionRequest;
import network.oxalis.ng.api.outbound.TransmissionResponse;
import network.oxalis.ng.api.persist.PayloadPersister;
import network.oxalis.ng.api.persist.ReceiptPersister;
import network.oxalis.ng.api.tag.Tag;
import network.oxalis.ng.as4.api.MessageIdGenerator;
import network.oxalis.ng.as4.common.DefaultMessageIdGenerator;
import network.oxalis.ng.as4.inbound.As4InboundModule;
import network.oxalis.ng.as4.outbound.As4MessageSenderFacade;
import network.oxalis.ng.commons.guice.GuiceModuleLoader;
import network.oxalis.ng.test.jetty.AbstractJettyServerTest;
import network.oxalis.vefa.peppol.common.model.*;
import network.oxalis.vefa.peppol.mode.Mode;
import network.oxalis.vefa.peppol.security.api.CertificateValidator;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.X509Certificate;

/**
 * Show how to use custom {@link PayloadPersister} and {@link ReceiptPersister} (not just {@link InboundService})
 * to get enriched MLS_TO/MLS_TYPE values
 */
public class MlsResolutionPersisterCaptureTest extends AbstractJettyServerTest {

    private final CapturingPayloadPersister payloadPersister = new CapturingPayloadPersister();
    private final CapturingReceiptPersister receiptPersister = new CapturingReceiptPersister();

    @Override
    public Injector getInjector() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        return Guice.createInjector(
                new As4InboundModule(),
                Modules.override(new GuiceModuleLoader()).with(new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(Key.get(MessageSender.class, Names.named("oxalis-as4")))
                                .to(As4MessageSenderFacade.class);
                        bind(PayloadPersister.class).toInstance(payloadPersister);
                        bind(ReceiptPersister.class).toInstance(receiptPersister);
                        bind(InboundService.class).toInstance(metadata -> { });
                        bind(MessageIdGenerator.class).toInstance(new DefaultMessageIdGenerator("test.com"));
                        bind(CertificateValidator.class).toInstance(CertificateValidator.EMPTY);
                        bind(Mode.class).toInstance(Mode.of(Mode.TEST));
                    }

                    @Provides
                    @Singleton
                    protected KeyStore getKeyStore() throws Exception {
                        KeyStore keyStore = KeyStore.getInstance("JKS");
                        try (InputStream is = getClass().getResourceAsStream("/mls/pop000723.jks")) {
                            keyStore.load(is, "changeit".toCharArray());
                        }
                        return keyStore;
                    }
                })
        );
    }

    @BeforeMethod
    public void reset() {
        payloadPersister.reset();
        receiptPersister.reset();
    }

    @Test
    public void payloadAndReceiptPersistersObserveEnrichedMlsValues() throws Exception {
        byte[] payload = loadResource("/mls/bis3-invoice-mls-both-present-valid.xml");

        MessageSender messageSender = injector.getInstance(Key.get(MessageSender.class, Names.named("oxalis-as4")));

        TransmissionResponse response = messageSender.send(new TransmissionRequest() {
            @Override
            public Endpoint getEndpoint() {
                return Endpoint.of(TransportProfile.AS4, URI.create("http://localhost:8080/as4"),
                        injector.getInstance(X509Certificate.class));
            }

            @Override
            public Header getHeader() {
                return Header.newInstance()
                        .sender(ParticipantIdentifier.of("0007:5567125082"))
                        .receiver(ParticipantIdentifier.of("0007:4455454480"))
                        .documentType(DocumentTypeIdentifier.of(
                                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##" +
                                        "urn:cen.eu:en16931:2017#compliant#urn:fdc:peppol.eu:2017:poacc:billing:3.0::2.1"))
                        .process(ProcessIdentifier.of("urn:fdc:peppol.eu:2017:poacc:billing:01:1.0"));
            }

            @Override
            public InputStream getPayload() {
                return new ByteArrayInputStream(payload);
            }

            @Override
            public Tag getTag() {
                return Tag.NONE;
            }
        });

        Assert.assertNotNull(response);

        // PayloadPersister.persist(TransmissionIdentifier, Header, InputStream)
        Assert.assertNotNull(payloadPersister.lastHeader, "PayloadPersister was never called");
        Assert.assertEquals(payloadPersister.lastHeader.getMlsToIdentifier(), MlsToIdentifier.of("0242:000723"));
        Assert.assertEquals(payloadPersister.lastHeader.getMlsTypeIdentifier(), MlsTypeIdentifier.of("ALWAYS_SEND"));

        // ReceiptPersister.persist(InboundMetadata, Path) -> InboundMetadata.getHeader()
        Assert.assertNotNull(receiptPersister.lastMetadata, "ReceiptPersister was never called");
        Assert.assertEquals(receiptPersister.lastMetadata.getHeader().getMlsToIdentifier(),
                MlsToIdentifier.of("0242:000723"));
        Assert.assertEquals(receiptPersister.lastMetadata.getHeader().getMlsTypeIdentifier(),
                MlsTypeIdentifier.of("ALWAYS_SEND"));
    }

    private byte[] loadResource(String classpathResource) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(classpathResource)) {
            Assert.assertNotNull(is, "Missing test resource: " + classpathResource);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(is, baos);
            return baos.toByteArray();
        }
    }

    static class CapturingPayloadPersister implements PayloadPersister {
        private volatile Header lastHeader;

        @Override
        public Path persist(TransmissionIdentifier transmissionIdentifier, Header header, InputStream inputStream) {
            this.lastHeader = header;
            return null;
        }

        void reset() {
            lastHeader = null;
        }
    }

    static class CapturingReceiptPersister implements ReceiptPersister {
        private volatile InboundMetadata lastMetadata;

        @Override
        public void persist(InboundMetadata inboundMetadata, Path payloadPath) {
            this.lastMetadata = inboundMetadata;
        }

        void reset() {
            lastMetadata = null;
        }
    }
}