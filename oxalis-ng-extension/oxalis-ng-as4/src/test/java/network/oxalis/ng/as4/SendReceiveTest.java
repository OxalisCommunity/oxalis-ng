package network.oxalis.ng.as4;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import lombok.SneakyThrows;
import lombok.Value;
import network.oxalis.ng.api.inbound.InboundMetadata;
import network.oxalis.ng.api.inbound.InboundService;
import network.oxalis.ng.api.model.TransmissionIdentifier;
import network.oxalis.ng.as4.outbound.As4MessageSenderFacade;
import network.oxalis.ng.api.outbound.MessageSender;
import network.oxalis.ng.api.outbound.TransmissionRequest;
import network.oxalis.ng.api.outbound.TransmissionResponse;
import network.oxalis.ng.api.persist.PayloadPersister;
import network.oxalis.ng.api.persist.ReceiptPersister;
import network.oxalis.ng.api.tag.Tag;
import network.oxalis.ng.as4.api.MessageIdGenerator;
import network.oxalis.ng.as4.common.DefaultMessageIdGenerator;
import network.oxalis.ng.as4.inbound.As4InboundModule;
import network.oxalis.ng.as4.outbound.As4ReceiptValidationInInterceptor;
import network.oxalis.ng.as4.util.PeppolConfiguration;
import network.oxalis.ng.commons.guice.GuiceModuleLoader;
import network.oxalis.ng.test.jetty.AbstractJettyServerTest;
import network.oxalis.vefa.peppol.common.model.*;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.BusFactory;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.phase.Phase;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.*;

public class SendReceiveTest extends AbstractJettyServerTest {

    private TemporaryFilePersister temporaryFilePersister = new TemporaryFilePersister();
    private TemporaryInboundService temporaryInboundService = new TemporaryInboundService();

    private byte[] firstPayload;
    private byte[] secondPayload;

    public SendReceiveTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/as4-peppol-bis-invoice-sbdh.xml");
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        IOUtils.copy(is, buffer);

        this.firstPayload = buffer.toByteArray();

        is = getClass().getResourceAsStream("/simple-sbd.xml");
        buffer = new ByteArrayOutputStream();

        IOUtils.copy(is, buffer);

        this.secondPayload = buffer.toByteArray();
    }

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
                        bind(ReceiptPersister.class).toInstance((m, p) -> {
                        });
                        bind(PayloadPersister.class).toInstance(temporaryFilePersister);
                        bind(InboundService.class).toInstance(temporaryInboundService);
                        bind(MessageIdGenerator.class).toInstance(new DefaultMessageIdGenerator("test.com"));
                        bind(As4ReceiptValidationInInterceptor.class).toInstance(
                                new As4ReceiptValidationInInterceptor(
                                        Phase.POST_PROTOCOL,
                                        As4ReceiptValidationInInterceptor.ValidationMode.LOGGING)
                        );
                    }
                })
        );
    }

    @BeforeClass
    public void addLoggingInterceptors() {
        Collection<Feature> features = BusFactory.getDefaultBus().getFeatures();
        features.add(new LoggingFeature());
        BusFactory.getDefaultBus().setFeatures(features);
    }

    @BeforeTest
    public void reset() {
        temporaryFilePersister.reset();
    }

    @Test
    public void full() throws Exception {

        MessageSender messageSender = injector.getInstance(Key.get(MessageSender.class, Names.named("oxalis-as4")));

        TransmissionResponse firstResponse = messageSender.send(new TransmissionRequest() {
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
                        .documentType(DocumentTypeIdentifier.of("urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0:#urn:www.peppol.eu:bis:peppol4a:ver1.0::2.0"))
                        .process(ProcessIdentifier.of("urn:www.cenbii.eu:profile:bii04:ver1.0"));
            }

            @Override
            public InputStream getPayload() {
                return new ByteArrayInputStream(firstPayload);
            }

            @Override
            public Tag getTag() {
                return new PeppolConfiguration() {
                    @Override
                    public List<String> getActions() {
                        return super.getActions();
                    }

                    @Override
                    public String getPartyIDType() {
                        return "TestPartyIdType";
                    }

                    @Override
                    public String getAgreementRef() {
                        return "TestAgreementRef";
                    }

                    @Override
                    public String getFromRole() {
                        return "TestFromRole";
                    }

                    @Override
                    public String getToRole() {
                        return "TestToRole";
                    }

                    @Override
                    public String getIdentifier() {
                        return super.getIdentifier();
                    }
                };
            }
        });

        Assert.assertNotNull(firstResponse);
        Assert.assertEquals(TransportProfile.AS4, firstResponse.getProtocol());

        Assert.assertEquals(temporaryFilePersister.size(), 1);
        Map<TemporaryFilePersister.Types, Object> dataFromFirstTransmission = temporaryFilePersister.getPersistedData(firstResponse.getTransmissionIdentifier());
        Assert.assertNotNull(dataFromFirstTransmission);

        Assert.assertEquals(firstResponse.getTransmissionIdentifier(), dataFromFirstTransmission.get(TemporaryFilePersister.Types.ID));
        Assert.assertNotNull(dataFromFirstTransmission.get(TemporaryFilePersister.Types.HEADER));
        Assert.assertEquals(firstPayload, (byte[]) dataFromFirstTransmission.get(TemporaryFilePersister.Types.PAYLOAD));

        // Perform a second transmission
        TransmissionResponse secondResponse = messageSender.send(new TransmissionRequest() {
            @Override
            public Endpoint getEndpoint() {
                return Endpoint.of(TransportProfile.AS4, URI.create("http://localhost:8080/as4"),
                        injector.getInstance(X509Certificate.class));
            }

            @Override
            public Header getHeader() {
                return Header.newInstance()
                        .sender(ParticipantIdentifier.of("9908:991825827"))
                        .receiver(ParticipantIdentifier.of("9908:991825827"))
                        .documentType(DocumentTypeIdentifier.of("urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0:#urn:test.com:bis:something_else:ver1.0::2.0", Scheme.of("TestSchema")))
                        .process(ProcessIdentifier.of("urn:www.cenbii.eu:profile:something_else:ver1.0", Scheme.of("TestSchema")));
            }

            @Override
            public InputStream getPayload() {
                return new ByteArrayInputStream(secondPayload);
            }

        });

        Assert.assertNotNull(secondResponse);
        Assert.assertEquals(TransportProfile.AS4, secondResponse.getProtocol());

        Assert.assertNotEquals(secondResponse.getTransmissionIdentifier(), firstResponse.getTransmissionIdentifier());

        Assert.assertEquals(temporaryFilePersister.size(), 2);
        Map<TemporaryFilePersister.Types, Object> dataFromSecondTransmission = temporaryFilePersister.getPersistedData(secondResponse.getTransmissionIdentifier());
        Assert.assertNotNull(dataFromSecondTransmission);

        Assert.assertEquals(secondResponse.getTransmissionIdentifier(), dataFromSecondTransmission.get(TemporaryFilePersister.Types.ID));
        Assert.assertNotNull(dataFromSecondTransmission.get(TemporaryFilePersister.Types.HEADER));
        Assert.assertEquals(secondPayload, (byte[]) dataFromSecondTransmission.get(TemporaryFilePersister.Types.PAYLOAD));
    }

    static class TemporaryFilePersister implements PayloadPersister {
        enum Types {
            ID, HEADER, PAYLOAD
        }

        private Map<String, Data> map = new TreeMap<>();

        @Override
        public Path persist(TransmissionIdentifier transmissionIdentifier, Header header, InputStream is) throws IOException {

            Path path = Files.createTempFile(transmissionIdentifier.getIdentifier(), "tmp");

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            IOUtils.copy(is, buffer);
            byte[] payload = buffer.toByteArray();

            try (FileOutputStream fileOutputStream = new FileOutputStream(path.toFile())) {
                fileOutputStream.write(payload);
            }

            map.put(transmissionIdentifier.getIdentifier(), new Data(path, header));

            return path;
        }

        public void reset() {
            map.clear();
        }

        public int size() {
            return map.size();
        }

        @SneakyThrows
        public EnumMap<Types, Object> getPersistedData(TransmissionIdentifier transmissionIdentifier) {
            Data data = map.get(transmissionIdentifier.getIdentifier());

            try (FileInputStream fi = new FileInputStream(data.path.toFile())) {
                EnumMap<Types, Object> map = new EnumMap<>(Types.class);
                map.put(Types.ID, transmissionIdentifier);
                map.put(Types.HEADER, data.header);
                map.put(Types.PAYLOAD, IOUtils.toByteArray(fi));
                return map;
            }
        }

        @Value
        private static class Data {
            private final Path path;
            private final Header header;
        }
    }

    static class TemporaryInboundService implements InboundService {
        @Override
        public void complete(InboundMetadata inboundMetadata) {
        }
    }
}
