package network.oxalis.ng.as4.outbound;

import com.google.inject.*;
import com.google.inject.name.Names;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.ng.as4.common.AS4Constants;
import network.oxalis.ng.as4.config.As4Conf;
import network.oxalis.ng.as4.util.CompressionUtil;
import network.oxalis.ng.as4.util.PeppolConfiguration;
import network.oxalis.ng.api.outbound.MessageSender;
import network.oxalis.ng.api.settings.Settings;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class As4OutboundModule extends AbstractModule {

    @Override
    protected void configure() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        bind(Key.get(MessageSender.class, Names.named("oxalis-as4")))
                .to(As4MessageSenderFacade.class);

        bind(Key.get(ExecutorService.class, Names.named("compression-pool")))
                .toProvider(() -> Executors.newFixedThreadPool(5)).in(Scopes.SINGLETON);

        bind(CompressionUtil.class);

        bind(MessagingProvider.class);

        bind(As4MessageSender.class);

        bind(TransmissionResponseConverter.class);
    }

    @Provides
    @Singleton
    public PeppolConfiguration getPeppolOutboundConfiguration(Settings<As4Conf> settings) {
        String type = settings.getString(As4Conf.TYPE);

        if (AS4Constants.CEF_CONNECTIVITY.equalsIgnoreCase(type)) {
            return new PeppolConfiguration() {

                @Override
                public String getPartyIDType() {
                    return "urn:oasis:names:tc:ebcore:partyid-type:unregistered";
                }

                @Override
                public String getAgreementRef() {
                    return null;
                }
            };
        }

        return new PeppolConfiguration();
    }

    @Singleton
    @Provides
    public As4ReceiptValidationInInterceptor getAs4ReceiptValidationInInterceptor(Settings<As4Conf> settings) {
        return new As4ReceiptValidationInInterceptor(settings);
    }
}
