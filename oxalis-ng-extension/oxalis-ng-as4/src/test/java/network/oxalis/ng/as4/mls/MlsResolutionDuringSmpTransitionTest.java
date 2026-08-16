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
import network.oxalis.ng.as4.mls.config.MlsResolutionPolicy;
import network.oxalis.ng.as4.mls.smp.MlsToSmpValidator;
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
import java.util.function.Function;

public class MlsResolutionDuringSmpTransitionTest extends AbstractJettyServerTest {
    private static final String SENDER_MAIN_ID = "000723";
    private static final String SAMPLE_RESOURCE = "/mls/bis3-invoice-mls-to-specific-usecase.xml";

    private static final String SPECIFIC_MLS_TO = "0242:" + SENDER_MAIN_ID + "-MLS";

    private final CapturingInboundService capturingInboundService = new CapturingInboundService();
    private final ConfigurableMlsToSmpValidator smpValidator = new ConfigurableMlsToSmpValidator();

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
                        bind(MlsToSmpValidator.class).toInstance(smpValidator);
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

                    @Provides
                    @Singleton
                    protected MlsResolutionPolicy getMlsResolutionPolicy() {
                        return new MlsResolutionPolicy(MlsTypeIdentifier.of("FAILURE_ONLY"), true);
                    }
                })
        );
    }

    @BeforeMethod
    public void reset() {
        capturingInboundService.reset();
        smpValidator.registered();
    }

    @Test
    public void mlsToNotYetRegisteredInSmpFallsBackDuringTransition() throws Exception {
        smpValidator.notYetRegistered();

        Header enriched = sendResource(SAMPLE_RESOURCE);

        Assert.assertEquals(enriched.getMlsToIdentifier(), MlsToIdentifier.of("0242:" + SENDER_MAIN_ID));
    }

    @Test
    public void mlsToRegisteredInSmpIsHonouredAfterTransition() throws Exception {
        smpValidator.registered();

        Header enriched = sendResource(SAMPLE_RESOURCE);

        Assert.assertEquals(enriched.getMlsToIdentifier(), MlsToIdentifier.of(SPECIFIC_MLS_TO));
    }

    @Test
    public void smpValidatorFailureDuringTransitionStillFallsBackGracefully() throws Exception {
        smpValidator.throwsUnexpectedly();

        Header enriched = sendResource(SAMPLE_RESOURCE);

        Assert.assertEquals(enriched.getMlsToIdentifier(), MlsToIdentifier.of("0242:" + SENDER_MAIN_ID));
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

    static final class ConfigurableMlsToSmpValidator implements MlsToSmpValidator {
        private volatile Function<MlsToIdentifier, Boolean> behaviour = id -> true;

        @Override
        public boolean isRegisteredForMls(MlsToIdentifier mlsToIdentifier) {
            return behaviour.apply(mlsToIdentifier);
        }

        void registered() {
            behaviour = id -> true;
        }

        void notYetRegistered() {
            behaviour = id -> false;
        }

        void throwsUnexpectedly() {
            behaviour = id -> {
                throw new IllegalStateException("simulated unexpected SMP client failure during transition");
            };
        }
    }
}
