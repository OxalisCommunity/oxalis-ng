package network.oxalis.ng.as4.mls.smp;

import network.oxalis.vefa.peppol.common.lang.EndpointNotFoundException;
import network.oxalis.vefa.peppol.common.model.Endpoint;
import network.oxalis.vefa.peppol.common.model.MlsToIdentifier;
import network.oxalis.vefa.peppol.lookup.LookupClient;
import network.oxalis.vefa.peppol.lookup.api.LookupException;
import network.oxalis.vefa.peppol.security.lang.PeppolSecurityException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class LookupClientMlsToSmpValidatorTest {

    private static final long CACHE_TIME_TO_LIVE_SECONDS = 300;

    private static final MlsToIdentifier MLS_TO = MlsToIdentifier.of("0242:987654");

    private LookupClient lookupClient;

    private LookupClientMlsToSmpValidator validator;

    @BeforeMethod
    public void setUp() {
        lookupClient = mock(LookupClient.class);
        validator = new LookupClientMlsToSmpValidator(lookupClient, CACHE_TIME_TO_LIVE_SECONDS);
    }

    @Test
    public void returnsTrueWhenEndpointFound() throws Exception {
        when(lookupClient.getEndpoint(any(), any(), any(), any())).thenReturn(mock(Endpoint.class));

        assertTrue(validator.isRegisteredForMls(MLS_TO));
    }

    @Test
    public void returnsFalseWhenParticipantNotRegisteredForMls() throws Exception {
        when(lookupClient.getEndpoint(any(), any(), any(), any()))
                .thenThrow(new EndpointNotFoundException("not registered for MLS document type"));

        assertFalse(validator.isRegisteredForMls(MLS_TO));
    }

    @Test
    public void returnsFalseWhenSmpLookupFails() throws Exception {
        when(lookupClient.getEndpoint(any(), any(), any(), any()))
                .thenThrow(new LookupException("SML/SMP unreachable"));

        assertFalse(validator.isRegisteredForMls(MLS_TO));
    }

    @Test
    public void returnsFalseWhenSmpResponseIsUntrusted() throws Exception {
        when(lookupClient.getEndpoint(any(), any(), any(), any()))
                .thenThrow(new PeppolSecurityException("untrusted certificate"));

        assertFalse(validator.isRegisteredForMls(MLS_TO));
    }

    @Test
    public void cachesRegisteredParticipant() throws Exception {
        when(lookupClient.getEndpoint(any(), any(), any(), any())).thenReturn(mock(Endpoint.class));

        assertTrue(validator.isRegisteredForMls(MLS_TO));
        assertTrue(validator.isRegisteredForMls(MlsToIdentifier.of("0242:987654")));

        verify(lookupClient, times(1)).getEndpoint(any(), any(), any(), any());
    }

    @Test
    public void cachesParticipantNotRegisteredForMls() throws Exception {
        when(lookupClient.getEndpoint(any(), any(), any(), any()))
                .thenThrow(new EndpointNotFoundException("not registered for MLS document type"));

        assertFalse(validator.isRegisteredForMls(MLS_TO));
        assertFalse(validator.isRegisteredForMls(MLS_TO));

        verify(lookupClient, times(1)).getEndpoint(any(), any(), any(), any());
    }

    @Test
    public void doesNotCacheTransientLookupFailure() throws Exception {
        when(lookupClient.getEndpoint(any(), any(), any(), any()))
                .thenThrow(new LookupException("SML/SMP unreachable"))
                .thenReturn(mock(Endpoint.class));

        assertFalse(validator.isRegisteredForMls(MLS_TO));
        assertTrue(validator.isRegisteredForMls(MLS_TO));

        verify(lookupClient, times(2)).getEndpoint(any(), any(), any(), any());
    }

    @Test
    public void doesNotCacheWhenTimeToLiveIsZero() throws Exception {
        validator = new LookupClientMlsToSmpValidator(lookupClient, 0);
        when(lookupClient.getEndpoint(any(), any(), any(), any())).thenReturn(mock(Endpoint.class));

        assertTrue(validator.isRegisteredForMls(MLS_TO));
        assertTrue(validator.isRegisteredForMls(MLS_TO));

        verify(lookupClient, times(2)).getEndpoint(any(), any(), any(), any());
    }
}
