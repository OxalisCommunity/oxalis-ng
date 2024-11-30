package network.oxalis.ng.as4.inbound;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import network.oxalis.ng.as4.lang.OxalisAs4Exception;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.*;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.soap.SOAPBinding;

@WebServiceProvider
@ServiceMode(value = Service.Mode.MESSAGE)
@BindingType(value = SOAPBinding.SOAP12HTTP_BINDING)
@Singleton
public class As4Provider implements Provider<SOAPMessage> {

    @Resource
    private WebServiceContext context;

    @Inject
    private As4InboundHandler handler;

    @Override
    public SOAPMessage invoke(SOAPMessage request) {
        MessageContext messageContext = context.getMessageContext();
        HttpServletResponse httpRes = (HttpServletResponse) messageContext.get(AbstractHTTPDestination.HTTP_RESPONSE);
        httpRes.setStatus(HttpServletResponse.SC_OK);

        try {
            return handler.handle(request, messageContext);
        } catch (OxalisAs4Exception e) {
            httpRes.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            throw new WebServiceException(e);
        }
    }
}
