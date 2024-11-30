package network.oxalis.ng.as4.common;

import java.util.ArrayList;

public class As4MessageProperties extends ArrayList<As4MessageProperty> {

    public boolean isMissing(String name) {
        return stream().noneMatch(p -> name.equals(p.getName()));
    }

    public String getValueByName(String name) {
        return stream().filter(p -> name.equals(p.getName())).findAny().map(As4MessageProperty::getValue).orElse(null);
    }
}
