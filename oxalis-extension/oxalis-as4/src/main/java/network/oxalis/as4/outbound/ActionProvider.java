package network.oxalis.as4.outbound;

import network.oxalis.vefa.peppol.common.model.DocumentTypeIdentifier;

public interface ActionProvider {

    String getAction(DocumentTypeIdentifier documentTypeIdentifier);
}
