package network.oxalis.as4.util;

import lombok.extern.slf4j.Slf4j;
import network.oxalis.as4.lang.OxalisAs4TransmissionException;
import network.oxalis.api.outbound.TransmissionRequest;
import network.oxalis.as4.outbound.ActionProvider;
import network.oxalis.vefa.peppol.common.model.ProcessIdentifier;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.apache.neethi.Policy;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.CollaborationInfo;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class PolicyService {

    private final ActionProvider actionProvider;

    public PolicyService(ActionProvider actionProvider) {
        this.actionProvider = actionProvider;
    }

    public Policy getPolicy() throws OxalisAs4TransmissionException {
        Bus bus = BusFactory.getThreadDefaultBus();
        return getPolicy(bus);
    }

    public Policy getPolicy(Bus bus) throws OxalisAs4TransmissionException {
        return getPolicy(getDefaultPolicy(), bus);
    }

    public Policy getPolicy(TransmissionRequest request) throws OxalisAs4TransmissionException {
        Bus bus = BusFactory.getThreadDefaultBus();
        return getPolicy(request, bus);
    }

    public Policy getPolicy(TransmissionRequest request, Bus bus) throws OxalisAs4TransmissionException {
        String action = actionProvider.getAction(request.getHeader().getDocumentType());
        ProcessIdentifier process = request.getHeader().getProcess();
        String service = process.getIdentifier();
        return getPolicy(getPolicyClasspath(action, service), bus);
    }

    public Policy getPolicy(CollaborationInfo collaborationInfo) throws OxalisAs4TransmissionException {
        Bus bus = BusFactory.getThreadDefaultBus();
        return getPolicy(collaborationInfo, bus);
    }

    public Policy getPolicy(CollaborationInfo collaborationInfo, Bus bus) throws OxalisAs4TransmissionException {
        return getPolicy(getPolicyClasspath(collaborationInfo.getAction(), collaborationInfo.getService().getValue()), bus);
    }

    private Policy getPolicy(String policyClasspath, Bus bus) throws OxalisAs4TransmissionException {
        try {
            log.debug("Policy classpath: {}", policyClasspath);
            InputStream policyStream = getClass().getResourceAsStream(policyClasspath);
            PolicyBuilder builder = bus.getExtension(PolicyBuilder.class);
            return builder.getPolicy(policyStream);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            throw new OxalisAs4TransmissionException("Failed to get WS Policy", e);
        }
    }

    protected String getPolicyClasspath(String action, String service) {
        return getDefaultPolicy();
    }

    protected String getDefaultPolicy() {
        return "/eDeliveryAS4Policy_BST.xml";
    }
}

