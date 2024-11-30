package network.oxalis.ng.as4.inbound;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.ng.as4.util.PolicyService;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.policy.PolicyOutInterceptor;

@Slf4j
@Singleton
public class SetPolicyOutInterceptor extends AbstractSetPolicyInterceptor {

    @Inject
    public SetPolicyOutInterceptor(PolicyService policyService) {
        super(Phase.SETUP, policyService);
        addBefore(PolicyOutInterceptor.class.getName());
    }
}
