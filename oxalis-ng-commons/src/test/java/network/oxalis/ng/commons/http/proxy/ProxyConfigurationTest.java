package network.oxalis.ng.commons.http.proxy;

import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Unit tests for {@link ProxyConfiguration}.
 */
class ProxyConfigurationTest {

    @Test
    void shouldCreateImmutableProxyConfiguration() {

        // Arrange
        ProxyConfiguration configuration = new ProxyConfiguration(
                "proxy.company.com",
                8080,
                "*.internal|localhost");

        // Assert
        assertEquals("proxy.company.com", configuration.getHost());
        assertEquals(8080, configuration.getPort());
        assertEquals("*.internal|localhost", configuration.getNonProxyHosts());
    }
}