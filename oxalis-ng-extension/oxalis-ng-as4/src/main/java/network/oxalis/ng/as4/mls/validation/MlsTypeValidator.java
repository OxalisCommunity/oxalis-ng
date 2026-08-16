package network.oxalis.ng.as4.mls.validation;

import network.oxalis.vefa.peppol.common.model.MlsTypeIdentifier;

public final class MlsTypeValidator {

    private static final String FAILURE_ONLY = "FAILURE_ONLY";
    private static final String ALWAYS_SEND = "ALWAYS_SEND";

    public ValidationResult validate(MlsTypeIdentifier mlsTypeIdentifier) {
        if (mlsTypeIdentifier == null) {
            return ValidationResult.invalid("MLS_TYPE is absent");
        }

        String value = mlsTypeIdentifier.getIdentifier();
        if (!FAILURE_ONLY.equals(value) && !ALWAYS_SEND.equals(value)) {
            return ValidationResult.invalid("MLS_TYPE value '" + value + "' is not one of "
                    + FAILURE_ONLY + ", " + ALWAYS_SEND);
        }

        return ValidationResult.valid();
    }
}
