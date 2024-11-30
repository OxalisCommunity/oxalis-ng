package network.oxalis.ng.as4.outbound;

import network.oxalis.ng.as4.common.As4MessageProperties;
import network.oxalis.ng.api.outbound.TransmissionRequest;

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
