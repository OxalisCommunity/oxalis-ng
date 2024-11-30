package network.oxalis.ng.as4.util;

import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import network.oxalis.ng.as4.lang.OxalisAs4Exception;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.UserMessage;
import org.w3.xmldsig.ReferenceType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.soap.SOAPHeader;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@UtilityClass
public class SOAPHeaderParser {

    private static final String NS_ALL = "*";
    private static final String MESSAGING = "Messaging";
    private static final String BST = "BinarySecurityToken";
    private static final String SIG = "Signature";
    private static final String SIG_VAL = "SignatureValue";
    private static final String SIG_INFO = "SignedInfo";
    private static final String NRI = "NonRepudiationInformation";
    private static final String KEY_INFO = "KeyInfo";
    private static final String REF = "Reference";
    private static final String DIGEST_VAL = "DigestValue";
    private static final JAXBContext JAXB_CONTEXT = Marshalling.getInstance();

    public static byte[] getAttachmentDigest(String refId, SOAPHeader header) throws OxalisAs4Exception {
        NodeList sigInfoNode = header.getElementsByTagNameNS(NS_ALL, SIG_INFO);

        if (sigInfoNode.getLength() != 1) {
            throw new OxalisAs4Exception(String.format("Expected one Signature elements in header, but found %d", sigInfoNode.getLength()));
        }

        Element sigInfoElement = (Element) sigInfoNode.item(0);

        NodeList refNodes = sigInfoElement.getElementsByTagNameNS(NS_ALL, REF);

        for (int i = 0; i < refNodes.getLength(); i++) {
            Element refElement = (Element) refNodes.item(i);
            if (refId.equals(refElement.getAttribute("URI"))) {
                NodeList digestValueNode = refElement.getElementsByTagNameNS(NS_ALL, DIGEST_VAL);
                return digestValueNode.item(0).getTextContent().getBytes(StandardCharsets.UTF_8);
            }
        }

        return null;
    }

    public static X509Certificate getSenderCertificate(SOAPHeader header) throws OxalisAs4Exception {
        NodeList sigNode = header.getElementsByTagNameNS(NS_ALL, SIG);
        if (sigNode.getLength() != 1) {
            throw new OxalisAs4Exception(String.format("Expected one Signature element in header, but found %d", sigNode.getLength()));
        }
        Element sigElement = (Element) sigNode.item(0);
        NodeList keyInfoNode = sigElement.getElementsByTagNameNS(NS_ALL, KEY_INFO);
        if (keyInfoNode.getLength() != 1) {
            throw new OxalisAs4Exception(String.format("Expected one KeyInfo child of Signature, but found %d", keyInfoNode.getLength()));
        }
        Element keyInfoElement = (Element) keyInfoNode.item(0);
        NodeList refNode = keyInfoElement.getElementsByTagNameNS(NS_ALL, REF);
        if (refNode == null || refNode.getLength() != 1) {
            throw new OxalisAs4Exception(("Zero or multiple Reference nodes under Signature->KeyInfo"));
        }
        String refUri = ((Element) refNode.item(0)).getAttribute("URI").replace("#", "");

        NodeList bstNodes = header.getElementsByTagNameNS(NS_ALL, BST);
        if (bstNodes != null) {
            for (int i = 0; i < bstNodes.getLength(); i++) {
                Element bstElem = (Element) bstNodes.item(i);

                if (bstElem.getAttributeNS("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id").equals(refUri)) {
                    try {
                        String pem = bstElem.getTextContent().replaceAll("[\r\n]+", "");
                        byte[] buf = Base64.getDecoder().decode(pem);
                        return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(buf));
                    } catch (CertificateException e) {
                        throw new OxalisAs4Exception("Could not create certificate from BinarySecurityToken", e);
                    }
                }
            }
        }

        return null;
    }

    public static byte[] getSignature(SOAPHeader header) throws OxalisAs4Exception {
        NodeList sigNode = header.getElementsByTagNameNS(NS_ALL, SIG);
        if (sigNode.getLength() != 1) {
            throw new OxalisAs4Exception(String.format("Expected one Signature element in header, but found %d", sigNode.getLength()));
        }
        Element sigElement = (Element) sigNode.item(0);
        NodeList sigValNode = sigElement.getElementsByTagNameNS(NS_ALL, SIG_VAL);
        if (sigValNode == null || sigValNode.getLength() != 1) {
            throw new OxalisAs4Exception("Zero or multiple SignatureValue elements in header");
        }
        return sigValNode.item(0).getTextContent().replace("\r\n", "").getBytes(StandardCharsets.UTF_8);
    }

    public static List<ReferenceType> getReferenceListFromSignedInfo(SOAPHeader header) throws OxalisAs4Exception {
        NodeList sigInfoNode = header.getElementsByTagNameNS(NS_ALL, SIG_INFO);
        if (sigInfoNode == null || sigInfoNode.getLength() != 1) {
            throw new OxalisAs4Exception("Zero or multiple SignedInfo elements in header");
        }
        Element sigInfoElement = (Element) sigInfoNode.item(0);
        return refListFromElement(sigInfoElement);
    }

    private static List<ReferenceType> refListFromElement(Element element) throws OxalisAs4Exception {
        NodeList refNodes = element.getElementsByTagNameNS(NS_ALL, REF);
        List<ReferenceType> referenceList = Lists.newArrayList();

        if (refNodes == null) {
            return Collections.emptyList();
        }

        try {
            Unmarshaller unmarshaller = JAXB_CONTEXT.createUnmarshaller();
            for (int i = 0; i < refNodes.getLength(); i++) {
                referenceList.add(unmarshaller.unmarshal(refNodes.item(i), ReferenceType.class).getValue());
            }
        } catch (JAXBException e) {
            throw new OxalisAs4Exception("Could not unmarshal reference node", e);
        }
        return referenceList;
    }

    public static UserMessage getUserMessage(SOAPHeader header) throws OxalisAs4Exception {
        Node messagingNode = header.getElementsByTagNameNS(NS_ALL, MESSAGING).item(0);

        try {
            Unmarshaller unmarshaller = JAXB_CONTEXT.createUnmarshaller();
            Messaging messaging = unmarshaller.unmarshal(messagingNode, Messaging.class).getValue();

            return messaging.getUserMessage().stream()
                    .findFirst()
                    .orElseThrow(() -> new OxalisAs4Exception("No UserMessage present in header"));
        } catch (JAXBException e) {
            throw new OxalisAs4Exception("Could not unmarshal Messaging node from header");
        }

    }
}
