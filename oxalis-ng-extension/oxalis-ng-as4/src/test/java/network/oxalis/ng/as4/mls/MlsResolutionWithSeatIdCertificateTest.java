package network.oxalis.ng.as4.mls;

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
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.X509Certificate;

public class MlsResolutionWithSeatIdCertificateTest extends AbstractJettyServerTest {

    private static final String SENDER_MAIN_ID = "000723";
    private final CapturingInboundService capturingInboundService = new CapturingInboundService();

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
                        bind(ReceiptPersister.class).toInstance((m, p) -> { });
                        bind(PayloadPersister.class).toInstance((ti, header, is) -> null);
                        bind(InboundService.class).toInstance(capturingInboundService);
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
        capturingInboundService.reset();
    }

    @Test
    public void bothPresentAndValidArePreserved() throws Exception {
        Header enriched = sendResource("/mls/bis3-invoice-mls-both-present-valid.xml");

        Assert.assertEquals(enriched.getMlsToIdentifier(), MlsToIdentifier.of("0242:" + SENDER_MAIN_ID));
        Assert.assertEquals(enriched.getMlsTypeIdentifier(), MlsTypeIdentifier.of("ALWAYS_SEND"));
    }

    @Test
    public void bothMissingAreDerivedAndDefaulted() throws Exception {
        Header enriched = sendResource("/mls/bis3-invoice-mls-both-absent.xml");

        Assert.assertEquals(enriched.getMlsToIdentifier(), MlsToIdentifier.of("0242:" + SENDER_MAIN_ID));
        Assert.assertEquals(enriched.getMlsTypeIdentifier(), MlsTypeIdentifier.of("FAILURE_ONLY"));
    }

    @Test
    public void mlsToMissingMlsTypePresentIsDerivedAndPreserved() throws Exception {
        Header enriched = sendResource("/mls/bis3-invoice-mls-type-only.xml");

        Assert.assertEquals(enriched.getMlsToIdentifier(), MlsToIdentifier.of("0242:" + SENDER_MAIN_ID));
        Assert.assertEquals(enriched.getMlsTypeIdentifier(), MlsTypeIdentifier.of("FAILURE_ONLY"));
    }

    @Test
    public void mlsToPresentMlsTypeMissingIsPreservedAndDefaulted() throws Exception {
        Header enriched = sendResource("/mls/bis3-invoice-mls-to-only.xml");

        Assert.assertEquals(enriched.getMlsToIdentifier(), MlsToIdentifier.of("0242:" + SENDER_MAIN_ID));
        Assert.assertEquals(enriched.getMlsTypeIdentifier(), MlsTypeIdentifier.of("FAILURE_ONLY"));
    }

    @Test
    public void bothPresentButInvalidAreIgnoredInFavourOfFallback() throws Exception {
        Header enriched = sendResource("/mls/bis3-invoice-mls-both-present-invalid.xml");

        Assert.assertEquals(enriched.getMlsToIdentifier(), MlsToIdentifier.of("0242:" + SENDER_MAIN_ID));
        Assert.assertEquals(enriched.getMlsTypeIdentifier(), MlsTypeIdentifier.of("FAILURE_ONLY"));
    }

    private Header sendResource(String classpathResource) throws Exception {
        byte[] payload = loadResource(classpathResource);

        MessageSender messageSender = injector.getInstance(Key.get(MessageSender.class, Names.named("oxalis-as4")));

        TransmissionResponse response = messageSender.send(new TransmissionRequest() {
            @Override
            public Endpoint getEndpoint() {
                return Endpoint.of(TransportProfile.AS4, URI.create("http://localhost:8080/as4"),
                        injector.getInstance(X509Certificate.class));
            }

            @Override
            public Header getHeader() {
                return envelopeHeader();
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
        Assert.assertEquals(response.getProtocol(), TransportProfile.AS4);

        Header enriched = capturingInboundService.getLastHeader();
        Assert.assertNotNull(enriched, "As4InboundHandler did not report completion via InboundService");
        return enriched;
    }

    private static Header envelopeHeader() {
        return Header.newInstance()
                .sender(ParticipantIdentifier.of("0007:5567125082"))
                .receiver(ParticipantIdentifier.of("0007:4455454480"))
                .documentType(DocumentTypeIdentifier.of(
                        "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##" +
                                "urn:cen.eu:en16931:2017#compliant#urn:fdc:peppol.eu:2017:poacc:billing:3.0::2.1"))
                .process(ProcessIdentifier.of("urn:fdc:peppol.eu:2017:poacc:billing:01:1.0"));
    }

    private byte[] loadResource(String classpathResource) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(classpathResource)) {
            Assert.assertNotNull(is, "Missing test resource: " + classpathResource);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(is, baos);
            return baos.toByteArray();
        }
    }

    static class CapturingInboundService implements InboundService {

        private volatile InboundMetadata lastMetadata;

        @Override
        public void complete(InboundMetadata inboundMetadata) {
            this.lastMetadata = inboundMetadata;
        }

        Header getLastHeader() {
            return lastMetadata == null ? null : lastMetadata.getHeader();
        }

        void reset() {
            lastMetadata = null;
        }
    }
}
