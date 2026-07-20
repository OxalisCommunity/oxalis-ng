package network.oxalis.ng.commons.http.proxy;

import lombok.Value;

@Value
public class ProxyConfiguration {
    String host;
    int port;
    String nonProxyHosts;
}