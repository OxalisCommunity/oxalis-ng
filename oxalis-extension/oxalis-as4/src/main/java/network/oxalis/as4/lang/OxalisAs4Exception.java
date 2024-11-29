package network.oxalis.as4.lang;

import network.oxalis.api.lang.OxalisException;
import network.oxalis.as4.util.AS4ErrorCode;

public class OxalisAs4Exception extends OxalisException implements AS4Error {

    private AS4ErrorCode errorCode = AS4ErrorCode.EBMS_0004;
    private AS4ErrorCode.Severity severity = AS4ErrorCode.Severity.ERROR;

    public OxalisAs4Exception(String message) {
        super(message);
    }

    public OxalisAs4Exception(String message, Throwable cause) {
        super(message, cause);
    }

    public OxalisAs4Exception(String message, AS4ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public OxalisAs4Exception(String message, AS4ErrorCode errorCode, AS4ErrorCode.Severity severity) {
        super(message);
        this.errorCode = errorCode;
        this.severity = severity;
    }

    public OxalisAs4Exception(String message, Throwable cause, AS4ErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public OxalisAs4Exception(String message, Throwable cause, AS4ErrorCode errorCode, AS4ErrorCode.Severity severity) {
        super(message, cause);
        this.errorCode = errorCode;
        this.severity = severity;
    }


    public AS4ErrorCode getErrorCode() {
        return errorCode;
    }

    public AS4ErrorCode.Severity getSeverity() {
        return severity;
    }

    @Override
    public Exception getException() {
        return this;
    }
}
