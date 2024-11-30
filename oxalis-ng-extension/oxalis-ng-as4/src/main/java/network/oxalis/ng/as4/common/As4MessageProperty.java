package network.oxalis.ng.as4.common;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode
public class As4MessageProperty {

    String name;
    String type;
    String value;

    public As4MessageProperty(String name, String value) {
        this(name, null, value);
    }

    public As4MessageProperty(String name, String type, String value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }
}
