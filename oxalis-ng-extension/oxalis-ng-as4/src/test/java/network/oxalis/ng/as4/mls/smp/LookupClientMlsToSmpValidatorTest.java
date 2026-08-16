package network.oxalis.ng.as4.mls.smp;

import network.oxalis.vefa.peppol.common.lang.EndpointNotFoundException;
import network.oxalis.vefa.peppol.common.model.Endpoint;
import network.oxalis.vefa.peppol.common.model.MlsToIdentifier;
import network.oxalis.vefa.peppol.lookup.LookupClient;
import network.oxalis.vefa.peppol.lookup.api.LookupException;
import network.oxalis.vefa.peppol.mode.Mode;
import network.oxalis.vefa.peppol.security.lang.PeppolSecurityException;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class LookupClientMlsToSmpValidatorTest {
    private final LookupClientMlsToSmpValidator validator = new LookupClientMlsToSmpValidator(Mode.of(Mode.TEST));

    @Test
    public void returnsTrueWhenEndpointFound() throws Exception {
        LookupClient lookupClient = mock(LookupClient.class);
        when(lookupClient.getEndpoint(any(), any(), any(), any())).thenReturn(mock(Endpoint.class));

        assertTrue(validator.isRegisteredForMls(MlsToIdentifier.of("0242:987654"), lookupClient));
    }

    @Test
    public void returnsFalseWhenParticipantNotRegisteredForMls() throws Exception {
        LookupClient lookupClient = mock(LookupClient.class);
        when(lookupClient.getEndpoint(any(), any(), any(), any()))
                .thenThrow(new EndpointNotFoundException("not registered for MLS document type"));

        assertFalse(validator.isRegisteredForMls(MlsToIdentifier.of("0242:987654"), lookupClient));
    }

    @Test
    public void returnsFalseWhenSmpLookupFails() throws Exception {
        LookupClient lookupClient = mock(LookupClient.class);
        when(lookupClient.getEndpoint(any(), any(), any(), any()))
                .thenThrow(new LookupException("SML/SMP unreachable"));

        assertFalse(validator.isRegisteredForMls(MlsToIdentifier.of("0242:987654"), lookupClient));
    }

    @Test
    public void returnsFalseWhenSmpResponseIsUntrusted() throws Exception {
        LookupClient lookupClient = mock(LookupClient.class);
        when(lookupClient.getEndpoint(any(), any(), any(), any()))
                .thenThrow(new PeppolSecurityException("untrusted certificate"));

        assertFalse(validator.isRegisteredForMls(MlsToIdentifier.of("0242:987654"), lookupClient));
    }
}
