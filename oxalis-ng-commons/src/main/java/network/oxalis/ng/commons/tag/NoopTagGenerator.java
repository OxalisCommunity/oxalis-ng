package network.oxalis.ng.commons.tag;

import network.oxalis.ng.api.model.Direction;
import network.oxalis.ng.api.tag.Tag;
import network.oxalis.ng.api.tag.TagGenerator;
import network.oxalis.ng.api.util.Type;

/**
 * @author erlend
 * @since 4.0.2
 */
@Type("noop")
public class NoopTagGenerator implements TagGenerator {

    @Override
    public Tag generate(Direction direction) {
        return Tag.NONE;
    }
}
