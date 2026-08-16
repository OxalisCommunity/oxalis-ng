package network.oxalis.ng.as4.mls.smp;

import network.oxalis.vefa.peppol.common.model.MlsToIdentifier;

public interface MlsToSmpValidator {

    boolean isRegisteredForMls(MlsToIdentifier mlsToIdentifier);
}
