package network.oxalis.ng.as4.lang;

import network.oxalis.ng.as4.util.AS4ErrorCode;

public interface AS4Error {

    AS4ErrorCode getErrorCode();

    AS4ErrorCode.Severity getSeverity();

    String getMessage();

    Exception getException();
}
