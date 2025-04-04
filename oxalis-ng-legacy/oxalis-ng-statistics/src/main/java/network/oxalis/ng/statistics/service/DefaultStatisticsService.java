/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package network.oxalis.ng.statistics.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.ng.api.inbound.InboundMetadata;
import network.oxalis.ng.api.model.AccessPointIdentifier;
import network.oxalis.ng.api.model.Direction;
import network.oxalis.ng.api.outbound.TransmissionRequest;
import network.oxalis.ng.api.outbound.TransmissionResponse;
import network.oxalis.ng.api.statistics.StatisticsService;
import network.oxalis.ng.api.util.Type;
import network.oxalis.ng.commons.security.CertificateUtils;
import network.oxalis.ng.commons.tracing.Traceable;
import network.oxalis.ng.statistics.api.ChannelId;
import network.oxalis.ng.statistics.api.RawStatisticsRepository;
import network.oxalis.ng.statistics.model.DefaultRawStatistics;

@Slf4j
@Singleton
@Type("default")
class DefaultStatisticsService extends Traceable implements StatisticsService {

    private final RawStatisticsRepository rawStatisticsRepository;

    @Inject
    public DefaultStatisticsService(RawStatisticsRepository rawStatisticsRepository, Tracer tracer) {
        super(tracer);
        this.rawStatisticsRepository = rawStatisticsRepository;
    }

    @Override
    public void persist(TransmissionRequest transmissionRequest, TransmissionResponse transmissionResponse) {
        Span span = tracer.spanBuilder("persist statistics").startSpan();
        try {
            String protocolName = transmissionRequest.getEndpoint().getTransportProfile().getIdentifier();
            String receivingAccessPointCommonName = transmissionRequest.getEndpoint().getCertificate() != null ? CertificateUtils
                    .extractCommonName(transmissionRequest.getEndpoint().getCertificate()) : "";

            DefaultRawStatistics rawStatistics = new DefaultRawStatistics.RawStatisticsBuilder()
                    .accessPointIdentifier(new AccessPointIdentifier(receivingAccessPointCommonName))
                    .direction(Direction.OUT)
                    .documentType(transmissionResponse.getHeader().getDocumentType())
                    .sender(transmissionResponse.getHeader().getSender())
                    .receiver(transmissionResponse.getHeader().getReceiver())
                    .profile(transmissionResponse.getHeader().getProcess())
                    .channel(new ChannelId(protocolName))
                    .date(transmissionResponse.getTimestamp())  // Time stamp of reception of the receipt
                    .build();

            rawStatisticsRepository.persist(rawStatistics);
        } catch (Exception ex) {
            span.setAttribute("exception", String.valueOf(ex.getMessage()));
            log.error("Persisting DefaultRawStatistics about outbound transmission failed : {}", ex.getMessage(), ex);
        } finally {
            span.end();
        }
    }

    @Override
    public void persist(InboundMetadata inboundMetadata) {
        // Persists raw statistics when message was received (ignore if stats couldn't be persisted, just warn)
        try {
            String protocolName = inboundMetadata.getProtocol().getIdentifier();
            String sendingAccessPointCommonName = CertificateUtils.extractCommonName(inboundMetadata.getCertificate());

            DefaultRawStatistics rawStatistics = new DefaultRawStatistics.RawStatisticsBuilder()
                    .accessPointIdentifier(new AccessPointIdentifier(sendingAccessPointCommonName))
                    .direction(Direction.IN)
                    .documentType(inboundMetadata.getHeader().getDocumentType())
                    .sender(inboundMetadata.getHeader().getSender())
                    .receiver(inboundMetadata.getHeader().getReceiver())
                    .profile(inboundMetadata.getHeader().getProcess())
                    .channel(new ChannelId(protocolName))
                    .build();

            rawStatisticsRepository.persist(rawStatistics);
        } catch (Exception e) {
            log.error("Unable to persist statistics for " + inboundMetadata.toString() + ";\n " + e.getMessage(), e);
        }
    }
}
