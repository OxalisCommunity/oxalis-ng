package network.oxalis.ng.server.jetty;

import network.oxalis.ng.api.settings.DefaultValue;
import network.oxalis.ng.api.settings.Nullable;
import network.oxalis.ng.api.settings.Path;
import network.oxalis.ng.api.settings.Title;

/**
 * @author erlend
 */
@Title("Jetty")
public enum JettyConf {

    @Path("oxalis.jetty.port")
    @DefaultValue("8080")
    PORT,

    @Path("oxalis.jetty.context_path")
    @DefaultValue("/")
    CONTEXT_PATH,

    @Path("oxalis.jetty.shutdown_token")
    @Nullable
    SHUTDOWN_TOKEN,

    @Path("oxalis.jetty.stop_timeout")
    @DefaultValue("10000")
    STOP_TIMEOUT,

}
