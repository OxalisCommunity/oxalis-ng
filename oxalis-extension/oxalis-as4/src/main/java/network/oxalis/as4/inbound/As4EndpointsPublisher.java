package network.oxalis.as4.inbound;

import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;

public interface As4EndpointsPublisher {

    EndpointImpl publish(Bus bus);
}
