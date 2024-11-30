package network.oxalis.ng.as4.inbound;

import com.google.inject.Key;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.wsdl.interceptors.AbstractEndpointSelectionInterceptor;

import jakarta.servlet.http.HttpServlet;

@Slf4j
public class As4InboundModule extends ServletModule {

    @Override
    protected void configureServlets() {
        bind(AbstractEndpointSelectionInterceptor.class).to(As4EndpointSelector.class);

        bind(Key.get(HttpServlet.class, Names.named("oxalis-as4")))
                .to(As4Servlet.class)
                .asEagerSingleton();

        bind(As4Provider.class);
        bind(As4EndpointsPublisher.class).to(As4EndpointsPublisherImpl.class);
        bind(As4InboundHandler.class);

        serve("/as4").with(Key.get(HttpServlet.class, Names.named("oxalis-as4")));
        serve("/as4/status").with(AS4StatusServlet.class);
    }
}
