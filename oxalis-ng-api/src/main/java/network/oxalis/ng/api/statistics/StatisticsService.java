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

package network.oxalis.ng.api.statistics;

import network.oxalis.ng.api.inbound.InboundMetadata;
import network.oxalis.ng.api.outbound.TransmissionRequest;
import network.oxalis.ng.api.outbound.TransmissionResponse;

/**
 * @author erlend
 * @since 4.0.0
 */
public interface StatisticsService {

    void persist(TransmissionRequest transmissionRequest, TransmissionResponse transmissionResponse);

    void persist(InboundMetadata inboundMetadata);
}
