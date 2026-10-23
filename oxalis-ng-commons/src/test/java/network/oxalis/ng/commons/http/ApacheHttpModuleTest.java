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

package network.oxalis.ng.commons.http;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.sun.net.httpserver.HttpServer;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import network.oxalis.ng.commons.guice.GuiceModuleLoader;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

@Guice(modules = {GuiceModuleLoader.class})
public class ApacheHttpModuleTest {

    @Inject
    private Provider<CloseableHttpClient> httpClientProvider;

    @Test
    public void simple() throws IOException {
        try (CloseableHttpClient httpClient1 = httpClientProvider.get();
             CloseableHttpClient httpClient2 = httpClientProvider.get()) {

            Assert.assertNotNull(httpClient1);
            Assert.assertNotNull(httpClient2);

            Assert.assertNotSame(httpClient1, httpClient2);
        }
    }

    @Test
    public void requestsAreTraced() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        });
        server.start();

        InMemorySpanExporter spanExporter = InMemorySpanExporter.create();

        try (SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
                .build();
             PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
             CloseableHttpClient httpClient = new ApacheHttpModule().getHttpClient(connectionManager,
                     RequestConfig.DEFAULT, OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build())) {

            String url = String.format("http://127.0.0.1:%d/", server.getAddress().getPort());
            int status = httpClient.execute(new HttpGet(url), response -> response.getCode());

            // Checked before the tracer provider closes, which clears the exporter
            List<SpanData> spans = spanExporter.getFinishedSpanItems();

            Assert.assertEquals(status, 204);
            Assert.assertEquals(spans.size(), 1);
            Assert.assertEquals(spans.get(0).getKind(), SpanKind.CLIENT);
            Assert.assertEquals(spans.get(0).getAttributes().get(AttributeKey.stringKey("http.request.method")), "GET");
        } finally {
            server.stop(0);
        }
    }
}
