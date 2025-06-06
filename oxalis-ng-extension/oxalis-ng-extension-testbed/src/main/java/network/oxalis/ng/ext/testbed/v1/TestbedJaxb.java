package network.oxalis.ng.ext.testbed.v1;

import network.oxalis.ng.ext.testbed.v1.jaxb.*;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

/**
 * @author erlend
 */
public class TestbedJaxb {

    public static final JAXBContext JAXB_CONTEXT;

    public static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    static {
        try {
            JAXB_CONTEXT = JAXBContext.newInstance(InformationType.class, OutboundType.class,
                    OutboundResponseType.class, InboundType.class, ErrorType.class);
        } catch (JAXBException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static Marshaller marshaller() throws JAXBException {
        return JAXB_CONTEXT.createMarshaller();
    }

    public static Unmarshaller unmarshaller() throws JAXBException {
        return JAXB_CONTEXT.createUnmarshaller();
    }

}
