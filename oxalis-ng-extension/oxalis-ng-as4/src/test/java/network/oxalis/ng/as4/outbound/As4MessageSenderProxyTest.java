package network.oxalis.ng.as4.outbound;

import network.oxalis.ng.as4.api.MessageIdGenerator;
import network.oxalis.ng.as4.common.MerlinProvider;
import network.oxalis.ng.as4.config.As4Conf;
import network.oxalis.ng.as4.util.CompressionUtil;
import network.oxalis.ng.as4.util.PolicyService;
import network.oxalis.ng.api.settings.Settings;
import network.oxalis.ng.commons.http.HttpConf;
import network.oxalis.ng.commons.security.KeyStoreConf;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

/**
 * Tests that {@link As4MessageSender#configureProxy(HTTPClientPolicy)} correctly maps
 * JVM proxy system properties to the CXF {@link HTTPClientPolicy}.
 */
public class As4MessageSenderProxyTest {

    @Mock private MessagingProvider messagingProvider;
    @Mock private MessageIdGenerator messageIdGenerator;
    @Mock private Settings<KeyStoreConf> keyStoreSettings;
    @Mock private Settings<As4Conf> as4Settings;
    @Mock private CompressionUtil compressionUtil;
    @Mock private Settings<HttpConf> httpConfSettings;
    @Mock private TransmissionResponseConverter responseConverter;
    @Mock private MerlinProvider merlinProvider;
    @Mock private PolicyService policyService;
    @Mock private BrowserTypeProvider browserTypeProvider;

    private As4MessageSender sender;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(browserTypeProvider.getBrowserType()).thenReturn("Oxalis-NG");
        sender = new As4MessageSender(
                messagingProvider, messageIdGenerator, keyStoreSettings,
                as4Settings, compressionUtil, httpConfSettings, responseConverter,
                merlinProvider, policyService, browserTypeProvider);
    }

    @After
    public void tearDown() {
        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");
        System.clearProperty("https.proxyHost");
        System.clearProperty("https.proxyPort");
        System.clearProperty("http.nonProxyHosts");
        System.clearProperty("https.nonProxyHosts");
    }

    @Test
    public void configureProxy_whenHttpsPropertiesSet_appliesProxyToPolicy() throws Exception {
        System.setProperty("https.proxyHost", "proxy.example.com");
        System.setProperty("https.proxyPort", "3128");
        System.setProperty("https.nonProxyHosts", "localhost|127.0.0.1|*.internal");

        HTTPClientPolicy policy = new HTTPClientPolicy();
        invokeConfigureProxy(policy);

        assertEquals("proxy.example.com", policy.getProxyServer());
        assertEquals(3128L, (long) policy.getProxyServerPort());
        assertEquals("localhost|127.0.0.1|*.internal", policy.getNonProxyHosts());
    }

    @Test
    public void configureProxy_whenOnlyHttpPropertiesSet_fallsBackToHttp() throws Exception {
        System.setProperty("http.proxyHost", "http-proxy.example.com");
        System.setProperty("http.proxyPort", "8080");

        HTTPClientPolicy policy = new HTTPClientPolicy();
        invokeConfigureProxy(policy);

        assertEquals("http-proxy.example.com", policy.getProxyServer());
        assertEquals(8080L, (long) policy.getProxyServerPort());
    }

    @Test
    public void configureProxy_whenHttpsAndHttpPropertiesSet_httpsProxyTakesPrecedence() throws Exception {
        System.setProperty("http.proxyHost", "http-proxy.example.com");
        System.setProperty("http.proxyPort", "8080");
        System.setProperty("https.proxyHost", "https-proxy.example.com");
        System.setProperty("https.proxyPort", "3128");

        HTTPClientPolicy policy = new HTTPClientPolicy();
        invokeConfigureProxy(policy);

        assertEquals("https-proxy.example.com", policy.getProxyServer());
        assertEquals(3128L, (long) policy.getProxyServerPort());
    }

    @Test
    public void configureProxy_whenNoProxyPropertiesSet_leavesProxyUnconfigured() throws Exception {
        HTTPClientPolicy policy = new HTTPClientPolicy();
        invokeConfigureProxy(policy);

        assertNull(policy.getProxyServer());
        assertEquals(0L, (long) policy.getProxyServerPort());
        assertNull(policy.getNonProxyHosts());
    }

    @Test
    public void configureProxy_whenNonProxyHostsNotSet_doesNotSetNonProxyHosts() throws Exception {
        System.setProperty("https.proxyHost", "proxy.example.com");
        System.setProperty("https.proxyPort", "3128");

        HTTPClientPolicy policy = new HTTPClientPolicy();
        invokeConfigureProxy(policy);

        assertEquals("proxy.example.com", policy.getProxyServer());
        assertNull(policy.getNonProxyHosts());
    }

    private void invokeConfigureProxy(HTTPClientPolicy policy) throws Exception {
        Method method = As4MessageSender.class.getDeclaredMethod("configureProxy", HTTPClientPolicy.class);
        method.setAccessible(true);
        method.invoke(sender, policy);
    }
}

