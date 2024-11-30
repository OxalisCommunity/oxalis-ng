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

package network.oxalis.ng.commons.persist;

import com.google.inject.Inject;
import network.oxalis.ng.api.inbound.InboundMetadata;
import network.oxalis.ng.api.model.TransmissionIdentifier;
import network.oxalis.ng.api.persist.ExceptionPersister;
import network.oxalis.ng.api.persist.PayloadPersister;
import network.oxalis.ng.api.persist.PersisterHandler;
import network.oxalis.ng.api.persist.ReceiptPersister;
import network.oxalis.ng.api.util.Type;
import network.oxalis.vefa.peppol.common.model.Header;

import jakarta.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * @author erlend
 * @since 4.0.0
 */
@Singleton
@Type("default")
public class DefaultPersisterHandler implements PersisterHandler {

    private PayloadPersister payloadPersister;

    private ReceiptPersister receiptPersister;

    private ExceptionPersister exceptionPersister;

    @Inject
    public DefaultPersisterHandler(PayloadPersister payloadPersister, ReceiptPersister receiptPersister,
                                   ExceptionPersister exceptionPersister) {
        this.payloadPersister = payloadPersister;
        this.receiptPersister = receiptPersister;
        this.exceptionPersister = exceptionPersister;
    }

    @Override
    public void persist(InboundMetadata inboundMetadata, Path payloadPath) throws IOException {
        receiptPersister.persist(inboundMetadata, payloadPath);
    }

    @Override
    public Path persist(TransmissionIdentifier transmissionIdentifier, Header header, InputStream inputStream)
            throws IOException {
        return payloadPersister.persist(transmissionIdentifier, header, inputStream);
    }

    @Override
    public void persist(TransmissionIdentifier transmissionIdentifier, Header header,
                        Path payloadPath, Exception exception) {
        exceptionPersister.persist(transmissionIdentifier, header, payloadPath, exception);
    }
}
