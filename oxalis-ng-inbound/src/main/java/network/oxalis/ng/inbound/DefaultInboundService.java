package network.oxalis.ng.inbound;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import network.oxalis.ng.api.inbound.InboundMetadata;
import network.oxalis.ng.api.inbound.InboundService;
import network.oxalis.ng.api.statistics.StatisticsService;

/**
 * @author erlend
 * @since 4.0.2
 */
@Singleton
public class DefaultInboundService implements InboundService {

    private StatisticsService statisticsService;

    @Inject
    public DefaultInboundService(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @Override
    public void complete(InboundMetadata inboundMetadata) {
        statisticsService.persist(inboundMetadata);
    }
}
