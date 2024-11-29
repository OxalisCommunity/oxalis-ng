package org.apache.cxf.attachment;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.interceptor.AttachmentInInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class As4AttachmentInInterceptor extends AbstractPhaseInterceptor<Message> {

    private static final Logger LOG = LogUtils.getL7dLogger(AttachmentInInterceptor.class);

    private static final List<String> TYPES = Collections.singletonList("multipart/related");

    public As4AttachmentInInterceptor() {
        super(Phase.RECEIVE);
    }

    public void handleMessage(Message message) {
        if (isGET(message)) {
            LOG.fine("As4AttachmentInInterceptor skipped in HTTP GET method");
            return;
        }
        if (message.getContent(InputStream.class) == null) {
            return;
        }

        String contentType = (String) message.get(Message.CONTENT_TYPE);
        if (AttachmentUtil.isTypeSupported(contentType, getSupportedTypes())) {
            As4AttachmentDeserializer ad = new As4AttachmentDeserializer(message, getSupportedTypes());
            try {
                ad.initializeAttachments();
            } catch (IOException e) {
                throw new Fault(e);
            }

            message.put(As4AttachmentDeserializer.class, ad);
        }
    }

    public void handleFault(Message messageParam) {
    }

    protected List<String> getSupportedTypes() {
        return TYPES;
    }
}

