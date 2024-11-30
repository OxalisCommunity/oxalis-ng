package network.oxalis.ng.commons.error;

import network.oxalis.ng.api.settings.DefaultValue;
import network.oxalis.ng.api.settings.Path;
import network.oxalis.ng.api.settings.Title;

/**
 * @author erlend
 * @since 4.0.2
 */
@Title("Error")
public enum ErrorConf {

    @Path("oxalis.error.handler")
    @DefaultValue("quiet")
    TRACKER,

}
