package network.oxalis.ng.as4.util;

import static network.oxalis.ng.as4.util.AS4ErrorCode.Category.*;
import static network.oxalis.ng.as4.util.AS4ErrorCode.Origin.EBMS;
import static network.oxalis.ng.as4.util.AS4ErrorCode.Origin.SECURITY;

public enum AS4ErrorCode {

    EBMS_0001("EBMS:0001", "ValueNotRecognized", CONTENT, EBMS),
    EBMS_0002("EBMS:0002", "FeatureNotSupported", CONTENT, EBMS),
    EBMS_0003("EBMS:0003", "ValueInconsistent", CONTENT, EBMS),
    EBMS_0004("EBMS:0004", "Other", CONTENT, EBMS),
    EBMS_0005("EBMS:0005", "ConnectionFailure", COMMUNICATION, EBMS),
    EBMS_0006("EBMS:0006", "EmptyMessagePartitionFlow", COMMUNICATION, EBMS),
    EBMS_0007("EBMS:0007", "MimeInconsistency", UNPACKAGING, EBMS),
    EBMS_0008("EBMS:0008", "FeatureNotSupported", UNPACKAGING, EBMS),
    EBMS_0009("EBMS:0009", "InvalidHeader", UNPACKAGING, EBMS),
    EBMS_0010("EBMS:0010", "ProcessingModeMismatch", PROCESSING, EBMS),

    EBMS_0101("EBMS:0101", "FailedAuthentication", PROCESSING, SECURITY),
    EBMS_0102("EBMS:0102", "FailedDecryption", PROCESSING, SECURITY),
    EBMS_0103("EBMS:0103", "PolicyNoncompliance", PROCESSING, SECURITY),
    EBMS_0201("EBMS:0201", "DysfunctionalReliability", PROCESSING, SECURITY),
    EBMS_0202("EBMS:0202", "DeliveryFailure", COMMUNICATION, SECURITY),

    // https://docs.oasis-open.org/ebxml-msg/ebms/v3.0/profiles/AS4-profile/v1.0/os/AS4-profile-v1.0-os.html [section 3.6]
    EBMS_0301("EBMS:0301", "MissingReceipt", COMMUNICATION, EBMS),
    EBMS_0302("EBMS:0302", "InvalidReceipt", COMMUNICATION, EBMS),
    EBMS_0303("EBMS:0303", "DecompressionFailure", COMMUNICATION, EBMS);

    private String errorCode;
    private String shortDescription;
    private Category catgory;
    private Origin origin;

    AS4ErrorCode(String errorCode, String shortDescription, Category category, Origin origin){
        this.errorCode = errorCode;
        this.shortDescription = shortDescription;
        this.catgory = category;
        this.origin = origin;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public Category getCatgory() {
        return catgory;
    }

    public Origin getOrigin() {
        return origin;
    }

    public static AS4ErrorCode nameOf(String name){
        for(AS4ErrorCode errorCode : AS4ErrorCode.values()){
            if (errorCode.toString().equalsIgnoreCase(name)){
                return errorCode;
            }
        }

        return null;
    }


    @Override
    public String toString() {
        return getErrorCode();
    }

    public enum Category{
        CONTENT,
        COMMUNICATION,
        UNPACKAGING,
        PROCESSING;

        @Override
        public String toString() {
            return name().substring(0,1).toUpperCase() + name().substring(1).toLowerCase();
        }
    }

    public enum Origin{
        EBMS,
        SECURITY,
        RELIABILITY;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    public enum Severity{
        ERROR,
        FAILURE,
        WARNING;

        @Override
        public String toString() {
            return name().toLowerCase();
        }

        public static Severity nameOf(String name){
            for(Severity severity : Severity.values()){
                if (severity.toString().equalsIgnoreCase(name)){
                    return severity;
                }
            }

            return null;
        }
    }
}
