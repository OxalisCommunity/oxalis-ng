package network.oxalis.ng.commons.lookup;

import com.google.inject.Inject;
import com.google.inject.Provider;
import network.oxalis.ng.commons.guice.GuiceModuleLoader;
import network.oxalis.vefa.peppol.lookup.LookupClient;
import network.oxalis.vefa.peppol.lookup.api.MetadataFetcher;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

@Guice(modules = {GuiceModuleLoader.class})
public class LookupClientModuleTest {

    @Inject
    private Provider<LookupClient> lookupClientProvider;

    @Inject
    private MetadataFetcher metadataFetcher;

    @Test
    public void lookupClientIsSingleton() {
        Assert.assertSame(lookupClientProvider.get(), lookupClientProvider.get());
    }

    @Test
    public void metadataFetcherUsesOxalisHttpClient() {
        Assert.assertTrue(metadataFetcher instanceof OxalisApacheFetcher);
    }
}
