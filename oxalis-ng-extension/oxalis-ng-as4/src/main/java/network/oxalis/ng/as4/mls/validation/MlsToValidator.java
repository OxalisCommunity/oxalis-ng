package network.oxalis.ng.as4.mls.validation;

import network.oxalis.vefa.peppol.common.model.MlsToIdentifier;
import java.util.regex.Pattern;

public final class MlsToValidator {

    private static final Pattern MLS_TO_PATTERN = Pattern.compile(
            "^0242:[0-9]{6}(-[0-9A-Z_]{3,12}(\\.[0-9A-Z\\-._~]{3,24})?)?$",
            Pattern.CASE_INSENSITIVE);

    public ValidationResult validate(MlsToIdentifier mlsToIdentifier) {
        if (mlsToIdentifier == null) {
            return ValidationResult.invalid("MLS_TO is absent");
        }

        if (mlsToIdentifier.getScheme() == null
                || mlsToIdentifier.getScheme().getIdentifier() == null
                || !mlsToIdentifier.getScheme().getIdentifier()
                        .equalsIgnoreCase(MlsToIdentifier.DEFAULT_SCHEME.getIdentifier())) {
            return ValidationResult.invalid(
                    "MLS_TO scheme must be '" + MlsToIdentifier.DEFAULT_SCHEME + "'");
        }

        String value = mlsToIdentifier.getIdentifier();
        if (value == null || !MLS_TO_PATTERN.matcher(value).matches()) {
            return ValidationResult.invalid(
                    "MLS_TO value '" + value + "' does not match the SPIS grammar");
        }

        return ValidationResult.valid();
    }
}
