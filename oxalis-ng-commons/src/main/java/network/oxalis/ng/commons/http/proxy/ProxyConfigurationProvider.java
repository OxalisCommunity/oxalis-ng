package network.oxalis.ng.commons.http.proxy;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.ng.commons.system.SystemPropertyProvider;

import java.util.Optional;

@Slf4j
@Singleton
public class ProxyConfigurationProvider {
    private final SystemPropertyProvider propertyProvider;

    @Inject
    public ProxyConfigurationProvider(
            SystemPropertyProvider propertyProvider) {
        this.propertyProvider = propertyProvider;
    }

    public Optional<ProxyConfiguration> get() {
        String host = getProperty(
                "https.proxyHost",
                "http.proxyHost");

        String port = getProperty(
                "https.proxyPort",
                "http.proxyPort");

        String nonProxyHosts = getProperty(
                "https.nonProxyHosts",
                "http.nonProxyHosts");

        if (host.isBlank() && port.isBlank()) {
            return Optional.empty();
        }

        if (host.isBlank() || port.isBlank()) {
            log.warn("Ignoring JVM proxy configuration because proxy host or proxy port is missing.");
            return Optional.empty();
        }

        final int proxyPort;

        try {
            proxyPort = Integer.parseInt(port);
        } catch (NumberFormatException ex) {
            log.warn("Ignoring JVM proxy configuration because proxy port '{}' is invalid.", port);
            return Optional.empty();
        }

        if (proxyPort < 1 || proxyPort > 65535) {
            log.warn("Ignoring JVM proxy configuration because proxy port '{}' is outside the valid TCP port range.", proxyPort);
            return Optional.empty();
        }

        return Optional.of(
                new ProxyConfiguration(
                        host,
                        proxyPort,
                        nonProxyHosts));
    }

    private String getProperty(
            String httpsProperty,
            String httpProperty) {

        String https = propertyProvider.getProperty(httpsProperty);
        if (https != null && !https.isBlank()) {
            return https;
        }
        String http = propertyProvider.getProperty(httpProperty);
        return http == null ? "" : http;
    }

}