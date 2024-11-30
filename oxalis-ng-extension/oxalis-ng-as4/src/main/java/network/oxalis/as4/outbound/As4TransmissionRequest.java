package network.oxalis.as4.outbound;

import network.oxalis.as4.common.As4MessageProperties;
import network.oxalis.api.outbound.TransmissionRequest;

import java.nio.charset.Charset;

public interface As4TransmissionRequest extends TransmissionRequest {

    String getRefToMessageId();

    String getMessageId();

    String getConversationId();

    As4MessageProperties getMessageProperties();

    String getPayloadHref();

    Charset getPayloadCharset();

    String getCompressionType();

    boolean isPing();
}
