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

package network.oxalis.ng.commons.evidence;

import com.google.common.io.ByteStreams;
import com.google.inject.Singleton;
import network.oxalis.ng.api.evidence.EvidenceFactory;
import network.oxalis.ng.api.transmission.TransmissionResult;
import network.oxalis.ng.api.util.Type;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Evidence factory writing only primary receipt to stream.
 *
 * @author erlend
 * @since 4.0.2
 */
@Singleton
@Type("primary")
public class PrimaryEvidenceFactory implements EvidenceFactory {

    @Override
    public void write(OutputStream outputStream, TransmissionResult transmissionResult) throws IOException {
        ByteStreams.copy(new ByteArrayInputStream(transmissionResult.primaryReceipt().getValue()), outputStream);
    }
}
