/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

package eu.peppol.outbound.transmission;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import eu.peppol.BusDoxProtocol;
import eu.peppol.identifier.PeppolDocumentTypeIdAcronym;
import eu.peppol.identifier.WellKnownParticipant;
import eu.peppol.outbound.TestLookupModule;
import eu.peppol.outbound.guice.TestResourceModule;
import eu.peppol.smp.SmpLookupManager;
import no.difi.vefa.peppol.common.model.*;
import no.difi.vefa.peppol.lookup.LookupClient;
import org.mockito.Mockito;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.security.cert.X509Certificate;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 29.10.13
 *         Time: 18:20
 */

@Guice(modules = {TransmissionTestModule.class, TestResourceModule.class, TestLookupModule.class})
public class MessageSenderFactoryTest {

    @Inject
    SmpLookupManager smpLookupManager;

    @Inject
    @Named("sample-xml-with-sbdh")
    InputStream sampleMessageInputStream;

    @Inject
    private LookupClient lookupClient;

    /**
     * Verifies that the internal method for obtaining information on the destination access point, works
     * as expected, i.e. should return AS2 for PPID U4_TEST due to the fact that the mock SmpLookupManager will
     * always return "AS2" for U4_TEST
     *
     * @throws Exception
     */
    @Test
    public void testProtocolObtained() throws Exception {
        Mockito.when(lookupClient.getEndpoint(Mockito.any(ParticipantIdentifier.class), Mockito.any(DocumentTypeIdentifier.class), Mockito.any(ProcessIdentifier.class), Mockito.eq(TransportProfile.AS2_1_0)))
                .thenReturn(Endpoint.of(Mockito.mock(ProcessIdentifier.class), TransportProfile.AS2_1_0, "http://...", Mockito.mock(X509Certificate.class)));

        SmpLookupManager.PeppolEndpointData endpointData = smpLookupManager.getEndpointTransmissionData(WellKnownParticipant.U4_TEST, PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier());
        assertNotNull(endpointData, "No endpoint data received");
        assertEquals(endpointData.getBusDoxProtocol(), BusDoxProtocol.AS2);
    }

}
