package network.oxalis.as4.inbound;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.as4.lang.OxalisAs4Exception;
import network.oxalis.as4.util.Constants;
import network.oxalis.as4.util.Marshalling;
import network.oxalis.as4.util.MessageId;
import network.oxalis.as4.util.PolicyService;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.headers.Header;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.policy.AssertionInfoMap;
import org.apache.cxf.ws.security.wss4j.PolicyBasedWSS4JStaxInInterceptor;
import org.apache.neethi.Policy;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.CollaborationInfo;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.MessageInfo;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.UserMessage;
import org.w3c.dom.Node;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Singleton
public class As4Interceptor extends AbstractSoapInterceptor {

    private final JAXBContext jaxbContext = Marshalling.getInstance();
    private final PolicyService policyService;

    @Inject
    public As4Interceptor(PolicyService policyService) {
        super(Phase.PRE_PROTOCOL);
        this.policyService = policyService;
        addBefore(PolicyBasedWSS4JStaxInInterceptor.class.getName());
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        Messaging messaging = getMessaging(message);

        storeMessageIdInContext(message, messaging);

        Optional<UserMessage> userMessage = Optional.ofNullable(messaging)
                .map(Messaging::getUserMessage)
                .map(Collection::stream).orElseGet(Stream::empty)
                .findFirst();

        try {
            Policy policy = userMessage.isPresent()
                    ? policyService.getPolicy(userMessage.get().getCollaborationInfo())
                    : policyService.getPolicy();
            message.put(AssertionInfoMap.class.getName(), new AssertionInfoMap(policy));
        } catch (Exception e) {
            throw new Fault(e);
        }
    }

    private Messaging getMessaging(Message message) {
        SoapMessage soapMessage = (SoapMessage) message;
        Header header = soapMessage.getHeader(Constants.MESSAGING_QNAME);

        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return unmarshaller.unmarshal((Node) header.getObject(), Messaging.class).getValue();
        } catch (JAXBException e) {
            throw new Fault(e);
        }
    }

    private void storeMessageIdInContext(Message message, Messaging messaging) throws Fault {
        String messageId = Optional.ofNullable(messaging)
                .map(Messaging::getUserMessage)
                .map(Collection::stream).orElseGet(Stream::empty)
                .map(UserMessage::getMessageInfo)
                .map(MessageInfo::getMessageId)
                .findFirst()
                .orElseThrow(() -> new Fault(new OxalisAs4Exception("MessageID is missing from UserMessage")));

        message.put(MessageId.MESSAGE_ID, new MessageId(messageId));

        Optional.ofNullable(messaging)
                .map(Messaging::getUserMessage)
                .map(Collection::stream).orElseGet(Stream::empty)
                .map(UserMessage::getCollaborationInfo)
                .map(CollaborationInfo::getConversationId)
                .findFirst()
                .ifPresent(conversationId -> message.put("oxalis.as4.conversationId", conversationId));
    }


}
