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

package network.oxalis.ng.sniffer.document.parsers;

import network.oxalis.ng.sniffer.document.PlainUBLParser;
import network.oxalis.vefa.peppol.common.model.ParticipantIdentifier;

/**
 * Parser to retrieves information from Peppol Despatch Advice scenarios.
 * Should be able to decode Despatch Advice document
 *
 * @author thore
 */
public class DespatchAdviceDocumentParser extends AbstractDocumentParser {

    public DespatchAdviceDocumentParser(PlainUBLParser parser) {
        super(parser);
    }

    @Override
    public ParticipantIdentifier getSender() {
        String despatchAdvice = "//cac:DespatchSupplierParty/cac:Party/cbc:EndpointID";
        return participantId(despatchAdvice);
    }

    @Override
    public ParticipantIdentifier getReceiver() {
        String despatchAdvice = "//cac:DeliveryCustomerParty/cac:Party/cbc:EndpointID";
        return participantId(despatchAdvice);
    }
}
