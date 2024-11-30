package network.oxalis.ng.as4.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MessageId {
    public static final String MESSAGE_ID = "oxalis.as4.messageId";

    private String value;
}
