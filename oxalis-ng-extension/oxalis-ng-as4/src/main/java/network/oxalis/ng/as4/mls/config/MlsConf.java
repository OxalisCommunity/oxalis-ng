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
    SMP_VALIDATION_ENABLED,

    /**
     * Seconds during which the SMP registration of an MLS_TO participant is cached. 0 disables the cache.
     */
    @Path("peppol.mls.smp.validation.cache.time_to_live")
    @DefaultValue("300")
    SMP_VALIDATION_CACHE_TIME_TO_LIVE
}
