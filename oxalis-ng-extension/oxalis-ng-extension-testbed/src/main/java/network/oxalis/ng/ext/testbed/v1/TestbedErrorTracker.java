package network.oxalis.ng.ext.testbed.v1;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import network.oxalis.ng.api.error.ErrorTracker;
import network.oxalis.ng.api.model.Direction;
import network.oxalis.ng.ext.testbed.v1.jaxb.ErrorType;

/**
 * @author erlend
 */
@Singleton
public class TestbedErrorTracker implements ErrorTracker {

    @Inject
    private TestbedSender sender;

    @Override
    public String track(Direction direction, Exception e, boolean handled) {
        ErrorType error = new ErrorType();
        error.setMessage(e.getMessage());
        sender.send(error);

        return null;
    }
}
