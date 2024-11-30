package network.oxalis.ng.ext.testbed.v1;

import com.google.inject.Key;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;
import network.oxalis.ng.api.error.ErrorTracker;
import network.oxalis.ng.api.persist.PersisterHandler;
import network.oxalis.ng.commons.settings.SettingsBuilder;

/**
 * @author erlend
 * @since 4.0.3
 */
public class TestbedModule extends ServletModule {

    @Override
    protected void configureServlets() {
        SettingsBuilder.with(binder(), TestbedConf.class);

        bind(Key.get(PersisterHandler.class, Names.named("testbed-v1")))
                .to(TestbedPersisterHandler.class);

        bind(Key.get(ErrorTracker.class, Names.named("testbed-v1")))
                .to(TestbedErrorTracker.class);

        serve("/testbed/v1").with(TestbedServlet.class);
        filter("/testbed/v1").through(TestbedFilter.class);
    }
}
