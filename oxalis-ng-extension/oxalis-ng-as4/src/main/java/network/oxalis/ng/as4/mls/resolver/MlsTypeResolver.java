package network.oxalis.ng.as4.mls.resolver;

import com.google.inject.Inject;
import network.oxalis.ng.as4.mls.config.MlsResolutionPolicy;
import network.oxalis.ng.as4.mls.validation.MlsTypeValidator;
import network.oxalis.vefa.peppol.common.model.Header;
import network.oxalis.vefa.peppol.common.model.MlsTypeIdentifier;

import java.util.Objects;

public final class MlsTypeResolver {

    private final MlsTypeValidator mlsTypeValidator;
    private final MlsResolutionPolicy policy;

    @Inject
    public MlsTypeResolver(MlsTypeValidator mlsTypeValidator, MlsResolutionPolicy policy) {
        this.mlsTypeValidator = Objects.requireNonNull(mlsTypeValidator, "mlsTypeValidator");
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    public Header resolve(Header header) {
        Objects.requireNonNull(header, "header");

        MlsTypeIdentifier existing = header.getMlsTypeIdentifier();
        if (mlsTypeValidator.validate(existing).isValid()) {
            return header;
        }

        return header.mlsTypeIdentifier(policy.getDefaultMlsType());
    }
}
