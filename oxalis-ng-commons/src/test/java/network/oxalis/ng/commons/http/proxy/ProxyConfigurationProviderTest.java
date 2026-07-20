package network.oxalis.ng.commons.http.proxy;

import network.oxalis.ng.commons.system.SystemPropertyProvider;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;


import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.*;

public class ProxyConfigurationProviderTest {

    @Mock
    private SystemPropertyProvider propertyProvider;

    private ProxyConfigurationProvider provider;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        provider = new ProxyConfigurationProvider(propertyProvider);
    }

    @Test
    public void shouldReturnEmptyWhenNoProxyConfigured() {

        Optional<ProxyConfiguration> configuration = provider.get();

        assertFalse(configuration.isPresent());
    }

    @Test
    public void shouldResolveHttpsProxy() {

        when(propertyProvider.getProperty("https.proxyHost"))
                .thenReturn("proxy.company.com");
        when(propertyProvider.getProperty("https.proxyPort"))
                .thenReturn("8080");

        Optional<ProxyConfiguration> configuration = provider.get();

        assertTrue(configuration.isPresent());

        assertEquals("proxy.company.com", configuration.get().getHost());
        assertEquals(8080, configuration.get().getPort());
        assertEquals("", configuration.get().getNonProxyHosts());
    }

    @Test
    public void shouldFallbackToHttpProxy() {

        when(propertyProvider.getProperty("http.proxyHost"))
                .thenReturn("proxy.company.com");
        when(propertyProvider.getProperty("http.proxyPort"))
                .thenReturn("8080");

        Optional<ProxyConfiguration> configuration = provider.get();

        assertTrue(configuration.isPresent());

        assertEquals("proxy.company.com", configuration.get().getHost());
        assertEquals(8080, configuration.get().getPort());
    }

    @Test
    public void shouldPreferHttpsOverHttp() {

        when(propertyProvider.getProperty("https.proxyHost"))
                .thenReturn("https-proxy");
        when(propertyProvider.getProperty("https.proxyPort"))
                .thenReturn("8443");

        when(propertyProvider.getProperty("http.proxyHost"))
                .thenReturn("http-proxy");
        when(propertyProvider.getProperty("http.proxyPort"))
                .thenReturn("8080");

        Optional<ProxyConfiguration> configuration = provider.get();

        assertTrue(configuration.isPresent());

        assertEquals("https-proxy", configuration.get().getHost());
        assertEquals(8443, configuration.get().getPort());
    }

    @Test
    public void shouldReturnEmptyWhenProxyHostMissing() {

        when(propertyProvider.getProperty("https.proxyPort"))
                .thenReturn("8080");

        Optional<ProxyConfiguration> configuration = provider.get();

        assertFalse(configuration.isPresent());
    }

    @Test
    public void shouldReturnEmptyWhenProxyPortMissing() {

        when(propertyProvider.getProperty("https.proxyHost"))
                .thenReturn("proxy.company.com");

        Optional<ProxyConfiguration> configuration = provider.get();

        assertFalse(configuration.isPresent());
    }

    @Test
    public void shouldReturnEmptyWhenProxyPortIsNotNumeric() {

        when(propertyProvider.getProperty("https.proxyHost"))
                .thenReturn("proxy.company.com");
        when(propertyProvider.getProperty("https.proxyPort"))
                .thenReturn("ABC");

        Optional<ProxyConfiguration> configuration = provider.get();

        assertFalse(configuration.isPresent());
    }

    @Test
    public void shouldReturnEmptyWhenProxyPortIsLessThanOne() {

        when(propertyProvider.getProperty("https.proxyHost"))
                .thenReturn("proxy.company.com");
        when(propertyProvider.getProperty("https.proxyPort"))
                .thenReturn("0");

        Optional<ProxyConfiguration> configuration = provider.get();

        assertFalse(configuration.isPresent());
    }

    @Test
    public void shouldReturnEmptyWhenProxyPortIsGreaterThan65535() {

        when(propertyProvider.getProperty("https.proxyHost"))
                .thenReturn("proxy.company.com");
        when(propertyProvider.getProperty("https.proxyPort"))
                .thenReturn("65536");

        Optional<ProxyConfiguration> configuration = provider.get();

        assertFalse(configuration.isPresent());
    }

    @Test
    public void shouldResolveNonProxyHosts() {

        when(propertyProvider.getProperty("https.proxyHost"))
                .thenReturn("proxy.company.com");
        when(propertyProvider.getProperty("https.proxyPort"))
                .thenReturn("8080");
        when(propertyProvider.getProperty("https.nonProxyHosts"))
                .thenReturn("localhost|*.internal");

        Optional<ProxyConfiguration> configuration = provider.get();

        assertTrue(configuration.isPresent());

        assertEquals(
                "localhost|*.internal",
                configuration.get().getNonProxyHosts());
    }
}