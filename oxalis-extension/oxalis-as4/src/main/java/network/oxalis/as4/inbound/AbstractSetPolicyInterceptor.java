package network.oxalis.as4.inbound;

import lombok.extern.slf4j.Slf4j;
import network.oxalis.as4.util.Constants;
import network.oxalis.as4.util.Marshalling;
import network.oxalis.as4.util.PolicyService;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.headers.Header;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.ws.policy.PolicyConstants;
import org.apache.neethi.Policy;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.UserMessage;
import org.w3c.dom.Node;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import static org.apache.cxf.ws.security.SecurityConstants.USE_ATTACHMENT_ENCRYPTION_CONTENT_ONLY_TRANSFORM;

@Slf4j
abstract class AbstractSetPolicyInterceptor extends AbstractSoapInterceptor {

    private final JAXBContext jaxbContext = Marshalling.getInstance();
    private final PolicyService policyService;

    public AbstractSetPolicyInterceptor(String phase, PolicyService policyService) {
        super(phase);
        this.policyService = policyService;
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        message.put(USE_ATTACHMENT_ENCRYPTION_CONTENT_ONLY_TRANSFORM, true);

        Optional<UserMessage> userMessage = getMessaging(message)
                .map(Messaging::getUserMessage)
                .map(Collection::stream).orElseGet(Stream::empty)
                .findFirst();

        try {
            Policy policy = userMessage.isPresent()
                    ? policyService.getPolicy(userMessage.get().getCollaborationInfo())
                    : policyService.getPolicy();
            message.put(PolicyConstants.POLICY_OVERRIDE, policy);
        } catch (Exception e) {
            throw new Fault(e);
        }
    }

    private Optional<Messaging> getMessaging(Message message) {
        SoapMessage soapMessage = (SoapMessage) message;
        Header header = soapMessage.getHeader(Constants.MESSAGING_QNAME);

        if (header == null) {
            return Optional.empty();
        }

        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            Messaging messaging = unmarshaller.unmarshal((Node) header.getObject(), Messaging.class).getValue();
            return Optional.of(messaging);
        } catch (JAXBException e) {
            throw new Fault(e);
        }
    }

}
