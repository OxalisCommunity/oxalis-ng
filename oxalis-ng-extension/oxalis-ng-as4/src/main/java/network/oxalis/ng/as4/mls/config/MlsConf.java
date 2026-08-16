package network.oxalis.ng.as4.mls.config;

import network.oxalis.ng.api.settings.DefaultValue;
import network.oxalis.ng.api.settings.Path;
import network.oxalis.ng.api.settings.Title;

@Title("MLS")
public enum MlsConf {

    @Path("peppol.mls.type.default")
    @DefaultValue("FAILURE_ONLY")
    TYPE_DEFAULT,

    @Path("peppol.mls.smp.validation.enabled")
    @DefaultValue("true")
    SMP_VALIDATION_ENABLED
}
