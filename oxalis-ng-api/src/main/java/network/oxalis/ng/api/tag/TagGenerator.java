package network.oxalis.ng.api.tag;

import network.oxalis.ng.api.model.Direction;

/**
 * @author erlend
 * @since 4.0.2
 */
public interface TagGenerator {

    default Tag generate(Direction direction, Tag original) {
        return original != Tag.NONE ? original : generate(direction);
    }

    Tag generate(Direction direction);

}
