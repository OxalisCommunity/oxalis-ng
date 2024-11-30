/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package network.oxalis.ng.as4.inbound;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import network.oxalis.ng.api.inbound.InboundService;
import network.oxalis.ng.commons.guice.GuiceModuleLoader;
import network.oxalis.ng.test.jetty.AbstractJettyServerTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import jakarta.servlet.http.HttpServletResponse;
import java.net.HttpURLConnection;
import java.net.URL;

@Test
public class AS4StatusServletTest extends AbstractJettyServerTest {

    @Override
    public Injector getInjector() {
        return Guice.createInjector(
                new As4InboundModule(),
                Modules.override(new GuiceModuleLoader()).with(new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(InboundService.class).toInstance(p -> {});
                    }
                })
        );
    }

    @Test
    public void get() throws Exception {
        HttpURLConnection httpURLConnection =
                (HttpURLConnection) new URL("http://localhost:8080/as4/status").openConnection();

        Assert.assertEquals(httpURLConnection.getResponseCode(), HttpServletResponse.SC_OK);
    }
}
