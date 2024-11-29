package network.oxalis.as4.inbound;

import lombok.SneakyThrows;
import org.apache.cxf.attachment.As4AttachmentDataSource;
import org.apache.cxf.attachment.As4AttachmentDeserializer;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import jakarta.activation.DataSource;

public class AttachmentCleanupInterceptor extends AbstractPhaseInterceptor<Message> {

    public AttachmentCleanupInterceptor() {
        super(Phase.POST_INVOKE);
    }

    public void handleMessage(Message message) throws Fault {
        Exchange exchange = message.getExchange();
        cleanRequestAttachment(exchange);
    }

    private void cleanRequestAttachment(Exchange exchange) {
        As4AttachmentDeserializer ad = exchange.getInMessage().get(As4AttachmentDeserializer.class);
        ad.getRemoved().forEach(this::close);
    }

    @SneakyThrows
    private void close(Attachment attachment) {
        DataSource dataSource = attachment.getDataHandler().getDataSource();

        if (dataSource instanceof As4AttachmentDataSource) {
            As4AttachmentDataSource ads = (As4AttachmentDataSource) dataSource;
            ads.closeAll();
        }
    }
}
