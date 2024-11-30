package network.oxalis.ng.outbound.transmission;

import network.oxalis.ng.api.outbound.TransmissionRequest;
import network.oxalis.ng.as4.api.MessageIdGenerator;
import network.oxalis.ng.as4.lang.OxalisAs4TransmissionException;
import network.oxalis.ng.as4.outbound.DefaultActionProvider;
import network.oxalis.ng.as4.outbound.MessagingProvider;
import network.oxalis.ng.as4.util.PeppolConfiguration;
import org.apache.cxf.message.Attachment;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;

import java.security.cert.X509Certificate;
import java.util.Collection;

public class MessagingProviderFacade {

    private MessagingProvider messagingProvider;

    public MessagingProviderFacade(X509Certificate senderCert, MessageIdGenerator messageIdGenerator, PeppolConfiguration peppolConfiguration) {
        messagingProvider = new MessagingProvider(
                senderCert,
                messageIdGenerator,
                peppolConfiguration,
                new DefaultActionProvider());
    }

    public Messaging createMessagingHeader(TransmissionRequest request, Collection<Attachment> attachments) throws OxalisAs4TransmissionException {
        return messagingProvider.createMessagingHeader(request, attachments);
    }
}
