package network.oxalis.ng.server.jetty;

import network.oxalis.ng.commons.guice.OxalisModule;

/**
 * @author erlend
 */
public class JettyModule extends OxalisModule {

    @Override
    protected void configure() {
        bindSettings(JettyConf.class);
    }
}
