package network.oxalis.ng.commons.error;

import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import network.oxalis.ng.api.error.ErrorTracker;
import network.oxalis.ng.api.settings.Settings;
import network.oxalis.ng.commons.guice.ImplLoader;
import network.oxalis.ng.commons.guice.OxalisModule;

/**
 * @author erlend
 * @since 4.0.2
 */
public class ErrorModule extends OxalisModule {

    @Override
    protected void configure() {
        bindTyped(ErrorTracker.class, FullErrorTracker.class);
        bindTyped(ErrorTracker.class, QuietErrorTracker.class);
        bindTyped(ErrorTracker.class, SilentErrorTracker.class);

        bindSettings(ErrorConf.class);
    }

    @Provides
    @Singleton
    protected ErrorTracker getErrorTracker(Injector injector, Settings<ErrorConf> settings) {
        return ImplLoader.get(injector, ErrorTracker.class, settings, ErrorConf.TRACKER);
    }
}
