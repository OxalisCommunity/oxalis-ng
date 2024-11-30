package network.oxalis.ng.commons.executor;

import network.oxalis.ng.api.settings.DefaultValue;
import network.oxalis.ng.api.settings.Path;
import network.oxalis.ng.api.settings.Title;

/**
 * @author erlend
 * @since 4.0.3
 */
@Title("Executor")
public enum ExecutorConf {

    @Path("oxalis.executor.default")
    @DefaultValue("50")
    DEFAULT,

    @Path("oxalis.executor.statistics")
    @DefaultValue("50")
    STATISTICS

}
