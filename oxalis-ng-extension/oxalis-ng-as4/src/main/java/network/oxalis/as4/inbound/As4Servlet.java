package network.oxalis.as4.inbound;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.as4.common.MerlinProvider;
import network.oxalis.api.settings.Settings;
import network.oxalis.commons.security.KeyStoreConf;
import org.apache.cxf.BusFactory;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.apache.cxf.ws.security.wss4j.PolicyBasedWSS4JInInterceptor;
import org.apache.cxf.ws.security.wss4j.PolicyBasedWSS4JOutInterceptor;
import org.apache.wss4j.common.crypto.Merlin;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.apache.cxf.rt.security.SecurityConstants.*;

@Slf4j
@Singleton
public class As4Servlet extends CXFNonSpringServlet {

    @Inject
    private Settings<KeyStoreConf> settings;

    @Inject
    private As4EndpointsPublisher endpointsPublisher;

    @Inject
    private MerlinProvider merlinProvider;

    @Override
    protected void loadBus(ServletConfig servletConfig) {
        this.bus = BusFactory.getThreadDefaultBus();

        EndpointImpl endpointImpl = endpointsPublisher.publish(getBus());

        Merlin merlin = merlinProvider.getMerlin();

        endpointImpl.getProperties().put(SIGNATURE_CRYPTO, merlin);
        endpointImpl.getProperties().put(SIGNATURE_PASSWORD, settings.getString(KeyStoreConf.KEY_PASSWORD));
        endpointImpl.getProperties().put(SIGNATURE_USERNAME, settings.getString(KeyStoreConf.KEY_ALIAS));

        endpointImpl.getProperties().put(ENCRYPT_CRYPTO, merlin);
        endpointImpl.getProperties().put(ENCRYPT_USERNAME, settings.getString(KeyStoreConf.KEY_ALIAS));

        endpointImpl.getInInterceptors().add(new PolicyBasedWSS4JInInterceptor());
        endpointImpl.getOutInterceptors().add(new PolicyBasedWSS4JOutInterceptor());

        endpointImpl.getFeatures().add(new LoggingFeature());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("Hello AS4 world\n");
        } catch (IOException e) {
            throw new ServletException("Unable to send response", e);
        }
    }
}
