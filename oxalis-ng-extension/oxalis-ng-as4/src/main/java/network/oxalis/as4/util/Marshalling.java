package network.oxalis.as4.util;

import lombok.experimental.UtilityClass;
import network.oxalis.peppol.sbdh.jaxb.StandardBusinessDocument;
import org.oasis_open.docs.ebxml_bp.ebbp_signals_2.NonRepudiationInformation;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import org.w3.xmldsig.ReferenceType;
import org.xmlsoap.schemas.soap.envelope.Envelope;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

@UtilityClass
public class Marshalling {

    public static JAXBContext getInstance() {
        return InitializedMarshaller.instance;
    }

    public static JAXBContext createMarshaller() {
        try {
            return JAXBContext.newInstance(
                    StandardBusinessDocument.class,
                    Envelope.class,
                    org.w3.soap.Envelope.class,
                    ReferenceType.class,
                    NonRepudiationInformation.class,
                    Messaging.class
            );
        } catch (JAXBException e) {
            throw new RuntimeException("Unable to create marshaller for AS4 documents", e);
        }
    }

    private static class InitializedMarshaller {
        private static final JAXBContext instance = createMarshaller();
    }
}

