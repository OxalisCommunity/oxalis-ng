package network.oxalis.ng.as4.mls.config;

import network.oxalis.vefa.peppol.common.model.MlsTypeIdentifier;

import java.util.Objects;

public final class MlsResolutionPolicy {
    private final MlsTypeIdentifier defaultMlsType;
    private final boolean smpValidationEnabled;

    public MlsResolutionPolicy(MlsTypeIdentifier defaultMlsType, boolean smpValidationEnabled) {
        this.defaultMlsType = Objects.requireNonNull(defaultMlsType, "defaultMlsType");
        this.smpValidationEnabled = smpValidationEnabled;
    }

    public MlsTypeIdentifier getDefaultMlsType() {
        return defaultMlsType;
    }

    public boolean isSmpValidationEnabled() {
        return smpValidationEnabled;
    }
}
