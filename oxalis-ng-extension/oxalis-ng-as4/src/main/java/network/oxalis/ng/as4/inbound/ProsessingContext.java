package network.oxalis.ng.as4.inbound;

import lombok.AllArgsConstructor;
import lombok.Getter;
import network.oxalis.ng.api.timestamp.Timestamp;
import org.w3.xmldsig.ReferenceType;

import java.util.List;

@Getter
@AllArgsConstructor
public class ProsessingContext {

    private Timestamp receiptTimestamp;
    private List<ReferenceType> referenceList;

}
