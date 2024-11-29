package network.oxalis.outbound.transmission;

import network.oxalis.as4.util.PeppolConfiguration;

public class MessagingProviderTest_SIMPLE_SBDH extends AbstractMessagingProviderTest {

    @Override
    protected String getPayloadPath() {
        return "/simple-sbd.xml";
    }

    @Override
    protected PeppolConfiguration getPEPPOLOutboundConfiguration() {

        return new PeppolConfiguration();
    }

    @Override
    protected String getAction() {
        return "busdox-docid-qns::urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biitrns010:ver2.0:extended:urn:www.peppol.eu:bis:peppol4a:ver2.0::2.1";
    }

    @Override
    protected String getServiceType() {
        return "cenbii-procid-ubl";
    }

    @Override
    protected String getServiceValue() {
        return "urn:www.cenbii.eu:profile:bii04:ver2.0";
    }

    @Override
    protected String getPartyType() {
        return "urn:fdc:peppol.eu:2017:identifiers:ap";
    }

    @Override
    protected String getFinalRecipient() {
        return "iso6523-actorid-upis::9908:810418052";
    }

    @Override
    protected String getOriginalSender() {
        return "iso6523-actorid-upis::0088:oxalis";
    }
}
