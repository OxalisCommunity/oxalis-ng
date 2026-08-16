package network.oxalis.ng.as4.mls.resolver;

import network.oxalis.ng.as4.mls.config.MlsResolutionPolicy;
import network.oxalis.ng.as4.mls.validation.MlsTypeValidator;
import network.oxalis.vefa.peppol.common.model.Header;
import network.oxalis.vefa.peppol.common.model.MlsTypeIdentifier;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class MlsTypeResolverTest {

    private final MlsResolutionPolicy policy = new MlsResolutionPolicy(MlsTypeIdentifier.of("FAILURE_ONLY"), false);

    private final MlsTypeResolver resolver = new MlsTypeResolver(new MlsTypeValidator(), policy);

    @Test
    public void preservesValidMlsType() {
        Header header = Header.newInstance().mlsTypeIdentifier(MlsTypeIdentifier.of("ALWAYS_SEND"));
        Header resolved = resolver.resolve(header);

        assertEquals(resolved.getMlsTypeIdentifier(), MlsTypeIdentifier.of("ALWAYS_SEND"));
    }

    @Test
    public void appliesDefaultWhenMissing() {
        Header header = Header.newInstance();
        Header resolved = resolver.resolve(header);

        assertEquals(resolved.getMlsTypeIdentifier(), MlsTypeIdentifier.of("FAILURE_ONLY"));
    }

    @Test
    public void appliesDefaultWhenInvalid() {
        Header header = Header.newInstance().mlsTypeIdentifier(MlsTypeIdentifier.of("BOGUS"));
        Header resolved = resolver.resolve(header);

        assertEquals(resolved.getMlsTypeIdentifier(), MlsTypeIdentifier.of("FAILURE_ONLY"));
    }
}
