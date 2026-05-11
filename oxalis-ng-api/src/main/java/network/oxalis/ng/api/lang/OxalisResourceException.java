package network.oxalis.ng.api.lang;

public class OxalisResourceException extends OxalisRuntimeException {
    public OxalisResourceException(String message) {
        super(message);
    }

    public OxalisResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
