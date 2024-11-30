package network.oxalis.as4.inbound;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.as4.util.PolicyService;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.policy.PolicyInInterceptor;

@Slf4j
@Singleton
public class SetPolicyInInterceptor extends AbstractSetPolicyInterceptor {

    @Inject
    public SetPolicyInInterceptor(PolicyService policyService) {
        super(Phase.RECEIVE, policyService);
        addBefore(PolicyInInterceptor.class.getName());
    }
}
