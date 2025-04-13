package network.oxalis.ng.outbound.lookup;

import network.oxalis.ng.api.settings.DefaultValue;
import network.oxalis.ng.api.settings.Path;
import network.oxalis.ng.api.settings.Title;

@Title("Lookup")
public enum LookupConf {

    @Path("oxalis.pint.wildcard.migration.phase")
    @DefaultValue("0")
    PINT_WILDCARD_MIGRATION_PHASE;

}
