package network.oxalis.ng.commons.error;

import network.oxalis.ng.api.error.ErrorTracker;
import network.oxalis.ng.api.model.Direction;
import network.oxalis.ng.api.util.Type;

import jakarta.inject.Singleton;
import java.util.UUID;

/**
 * Silent error tracker with no logging and returning untracked identifiers.
 *
 * @author erlend
 * @since 4.0.2
 */
@Type("silent")
@Singleton
public class SilentErrorTracker implements ErrorTracker {

    @Override
    public String track(Direction direction, Exception e, boolean handled) {
        // No logging.
        return String.format("untracked:%s", UUID.randomUUID().toString());
    }
}
