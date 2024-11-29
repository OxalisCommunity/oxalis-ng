package network.oxalis.as4.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.Bus;
import org.apache.cxf.ws.policy.AssertionBuilderRegistry;
import org.apache.cxf.ws.policy.builder.primitive.PrimitiveAssertion;
import org.apache.cxf.ws.policy.builder.primitive.PrimitiveAssertionBuilder;
import org.apache.cxf.ws.security.policy.custom.AlgorithmSuiteLoader;
import org.apache.neethi.Assertion;
import org.apache.neethi.AssertionBuilderFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.builders.xml.XMLPrimitiveAssertionBuilder;
import org.apache.wss4j.common.WSS4JConstants;
import org.apache.wss4j.policy.SPConstants;
import org.apache.wss4j.policy.model.AbstractSecurityAssertion;
import org.apache.wss4j.policy.model.AlgorithmSuite;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.wss4j.common.WSS4JConstants.MGF_SHA256;


// Reference from CEF e-delivery Domibus Implementation:
// https://ec.europa.eu/digital-building-blocks/code/projects/EDELIVERY/repos/domibus/browse/Domibus-MSH/src/main/java/eu/domibus/core/ebms3/ws/algorithm/DomibusAlgorithmSuiteLoader.java
@Slf4j
public class OxalisAlgorithmSuiteLoader implements AlgorithmSuiteLoader {

    public static final String OXALIS_ALGORITHM_NAMESPACE = "http://oxalis.network/custom/security-policy";
    public static final String AES128_GCM_ALGORITHM = "http://www.w3.org/2009/xmlenc11#aes128-gcm";
    public static final String BASIC_128_GCM_SHA_256 = "Basic128GCMSha256";
    public static final String BASIC_128_GCM_SHA_256_MGF_SHA_256 = "Basic128GCMSha256MgfSha256";

    private static final Map<String, Bus> BUS_MAP = new ConcurrentHashMap<>();

    public OxalisAlgorithmSuiteLoader(final Bus bus) {
        BUS_MAP.computeIfAbsent(bus.getId(), id -> {
            AlgorithmSuiteLoader algorithmSuiteLoader = bus.getExtension(AlgorithmSuiteLoader.class);

            if (algorithmSuiteLoader instanceof OxalisAlgorithmSuiteLoader) {
                log.info("Cached OxalisAlgorithmSuite on bus {}", bus.getId());
            } else {
                log.info("Registering OxalisAlgorithmSuite on bus {}", bus.getId());
                bus.setExtension(this, AlgorithmSuiteLoader.class);
                register(bus);
            }

            return bus;
        });
    }

    public AlgorithmSuite getAlgorithmSuite(final Bus bus, final SPConstants.SPVersion version, final Policy nestedPolicy) {
        return new OxalisAlgorithmSuite(version, nestedPolicy);
    }

    private void register(final Bus bus) {
        final AssertionBuilderRegistry reg = bus.getExtension(AssertionBuilderRegistry.class);
        if (reg != null) {
            final Map<QName, Assertion> assertions = new HashMap<>();
            QName qName = new QName(OXALIS_ALGORITHM_NAMESPACE, BASIC_128_GCM_SHA_256);
            assertions.put(qName, new PrimitiveAssertion(qName));
            qName = new QName(OXALIS_ALGORITHM_NAMESPACE, BASIC_128_GCM_SHA_256_MGF_SHA_256);
            assertions.put(qName, new PrimitiveAssertion(qName));

            reg.registerBuilder(new PrimitiveAssertionBuilder(assertions.keySet()) {
                @Override
                public Assertion build(final Element element, final AssertionBuilderFactory fact) {
                    if (XMLPrimitiveAssertionBuilder.isOptional(element)
                            || XMLPrimitiveAssertionBuilder.isIgnorable(element)) {
                        return super.build(element, fact);
                    }
                    final QName q = new QName(element.getNamespaceURI(), element.getLocalName());
                    return assertions.get(q);
                }
            });
        }
    }

    public static class OxalisAlgorithmSuite extends AlgorithmSuite {

        static {
            ALGORITHM_SUITE_TYPES.put(
                    BASIC_128_GCM_SHA_256,
                    new AlgorithmSuiteType(
                            BASIC_128_GCM_SHA_256,
                            SPConstants.SHA256,
                            OxalisAlgorithmSuiteLoader.AES128_GCM_ALGORITHM,
                            SPConstants.KW_AES128,
                            SPConstants.KW_RSA_OAEP,
                            SPConstants.P_SHA1_L128,
                            SPConstants.P_SHA1_L128,
                            128, 128, 128, 256, 1024, 4096
                    )
            );

            ALGORITHM_SUITE_TYPES.put(
                    BASIC_128_GCM_SHA_256_MGF_SHA_256,
                    new AlgorithmSuiteType(
                            BASIC_128_GCM_SHA_256_MGF_SHA_256,
                            SPConstants.SHA256,
                            OxalisAlgorithmSuiteLoader.AES128_GCM_ALGORITHM,
                            SPConstants.KW_AES128,
                            WSS4JConstants.KEYTRANSPORT_RSAOAEP_XENC11,
                            SPConstants.P_SHA1_L128,
                            SPConstants.P_SHA1_L128,
                            128, 128, 128, 256, 1024, 4096
                    )
            );
            ALGORITHM_SUITE_TYPES.get(BASIC_128_GCM_SHA_256_MGF_SHA_256).setMGFAlgo(MGF_SHA256);
            ALGORITHM_SUITE_TYPES.get(BASIC_128_GCM_SHA_256_MGF_SHA_256).setEncryptionDigest(SPConstants.SHA256);
        }

        OxalisAlgorithmSuite(final SPConstants.SPVersion version, final Policy nestedPolicy) {
            super(version, nestedPolicy);
        }

        @Override
        protected AbstractSecurityAssertion cloneAssertion(final Policy nestedPolicy) {
            return new OxalisAlgorithmSuite(this.getVersion(), nestedPolicy);
        }

        @Override
        protected void parseCustomAssertion(final Assertion assertion) {
            final String assertionName = assertion.getName().getLocalPart();
            final String assertionNamespace = assertion.getName().getNamespaceURI();
            if (!OxalisAlgorithmSuiteLoader.OXALIS_ALGORITHM_NAMESPACE.equals(assertionNamespace)) {
                return;
            }

            if (BASIC_128_GCM_SHA_256.equals(assertionName)) {
                setAlgorithmSuiteType(ALGORITHM_SUITE_TYPES.get(BASIC_128_GCM_SHA_256));
                getAlgorithmSuiteType().setNamespace(assertionNamespace);
                getAlgorithmSuiteType().setAsymmetricSignature("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");
            } else if (BASIC_128_GCM_SHA_256_MGF_SHA_256.equals(assertionName)) {
                setAlgorithmSuiteType(ALGORITHM_SUITE_TYPES.get(BASIC_128_GCM_SHA_256_MGF_SHA_256));
                getAlgorithmSuiteType().setNamespace(assertionNamespace);
                getAlgorithmSuiteType().setAsymmetricSignature("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");
            }
        }
    }
}