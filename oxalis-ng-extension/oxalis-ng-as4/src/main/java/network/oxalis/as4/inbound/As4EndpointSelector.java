package network.oxalis.as4.inbound;

import org.apache.cxf.binding.soap.interceptor.ReadHeadersInterceptor;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.wsdl.interceptors.AbstractEndpointSelectionInterceptor;

import java.util.Set;

public class As4EndpointSelector extends AbstractEndpointSelectionInterceptor {

    public static final String ENDPOINT_NAME = "Endpoint-name";
    public static final String OXALIS_AS4_ENDPOINT_NAME = "Oxalis-AS4";

    public As4EndpointSelector() {
        super(Phase.READ);
        getAfter().add(ReadHeadersInterceptor.class.getName());
    }

    @Override
    protected Endpoint selectEndpoint(Message message, Set<Endpoint> endpoints) {

        for (Endpoint endpoint : endpoints) {
            if (OXALIS_AS4_ENDPOINT_NAME.equals(endpoint.get(ENDPOINT_NAME))) {
                return endpoint;
            }
        }

        return null;
    }
}
