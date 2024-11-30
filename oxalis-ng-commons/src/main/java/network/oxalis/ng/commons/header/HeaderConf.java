package network.oxalis.ng.commons.header;

import network.oxalis.ng.api.settings.DefaultValue;
import network.oxalis.ng.api.settings.Path;
import network.oxalis.ng.api.settings.Title;

/**
 * @author erlend
 * @since 4.0.2
 */
@Title("Header")
public enum HeaderConf {

    @Path("oxalis.header.parser")
    @DefaultValue("sbdh")
    PARSER
}
