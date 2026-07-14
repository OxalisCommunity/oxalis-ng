package network.oxalis.ng.as4.util;

import lombok.experimental.UtilityClass;

import javax.xml.namespace.QName;

@UtilityClass
public class Constants {

    public static final String EBMS_NAMESPACE = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/";
    public static final String EBBP_SIGNALS_NAMESPACE = "http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0";
    public static final String DSIG_NAMESPACE = "http://www.w3.org/2000/09/xmldsig#";
    public static final String SOAP12_ENV_NAMESPACE = "http://www.w3.org/2003/05/soap-envelope";
    public static final String SBDH_NAMESPACE = "http://www.unece.org/cefact/namespaces/StandardBusinessDocumentHeader";

    public static final QName MESSAGING_QNAME = new QName(EBMS_NAMESPACE, "Messaging", "eb");
    public static final QName USER_MESSAGE_QNAME = new QName(EBMS_NAMESPACE, "UserMessage");
    public static final QName SIGNAL_MESSAGE_QNAME = new QName(EBMS_NAMESPACE, "SignalMessage");

    public static final String DIGEST_ALGORITHM_SHA256 = "sha256";

    public static final String TEST_SERVICE = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/service";
    public static final String TEST_ACTION = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/test";
}
