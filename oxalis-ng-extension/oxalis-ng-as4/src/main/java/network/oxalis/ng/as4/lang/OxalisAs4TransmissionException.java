package network.oxalis.ng.as4.lang;

import network.oxalis.ng.api.lang.OxalisTransmissionException;
import network.oxalis.ng.as4.util.AS4ErrorCode;

import java.net.URI;

public class OxalisAs4TransmissionException extends OxalisTransmissionException implements AS4Error {

    private AS4ErrorCode errorCode = AS4ErrorCode.EBMS_0004;
    private AS4ErrorCode.Severity severity = AS4ErrorCode.Severity.ERROR;

    public OxalisAs4TransmissionException(String message) {
        super(message);
    }

    public OxalisAs4TransmissionException(String message, AS4ErrorCode errorCode, AS4ErrorCode.Severity severity) {
        super(message);
        this.errorCode = errorCode;
        this.severity = severity;
    }

    public OxalisAs4TransmissionException(String message, Throwable cause) {
        super(message, cause);
    }

    public OxalisAs4TransmissionException(URI url, Throwable cause) {
        super(url, cause);
    }

    public OxalisAs4TransmissionException(String msg, URI url, Throwable e) {
        super(msg, url, e);
    }

    @Override
    public AS4ErrorCode getErrorCode() {
        return errorCode;
    }

    @Override
    public AS4ErrorCode.Severity getSeverity() {
        return severity;
    }

    @Override
    public Exception getException() {
        return this;
    }
}
