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

package network.oxalis.ng.sniffer.identifier;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

/**
 * @author Steinar Overbeck Cook steinar@sendregning.no
 */
public class DocumentTypeIdentifierTest {

    @Test
    public void testValueOf() {
        String documentIdAsText =
                "urn:oasis:names:specification:ubl:schema:xsd:ApplicationResponse-2::ApplicationResponse" +
                        "##urn:www.cenbii.eu:transaction:biicoretrdm057:ver1.0" +
                        ":#urn:www.peppol.eu:bis:peppol1a:ver1.0" +
                        "::2.0";
        PeppolDocumentTypeId documentTypeIdentifier = PeppolDocumentTypeId.valueOf(documentIdAsText);
        assertEquals(documentTypeIdentifier.toString(), documentIdAsText);

        assertEquals(documentTypeIdentifier.getRootNameSpace(),
                "urn:oasis:names:specification:ubl:schema:xsd:ApplicationResponse-2");
        assertEquals(documentTypeIdentifier.getLocalName(), "ApplicationResponse");
        assertEquals(documentTypeIdentifier.getCustomizationIdentifier(), CustomizationIdentifier.valueOf(
                "urn:www.cenbii.eu:transaction:biicoretrdm057:ver1.0" +
                        ":#urn:www.peppol.eu:bis:peppol1a:ver1.0"));
        assertEquals(documentTypeIdentifier.getVersion(), "2.0");
    }

    @Test
    public void equals() {
        String s = "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote" +
                "##urn:www.cenbii.eu:transaction:biicoretrdm014:ver1.0" +
                ":#urn:www.cenbii.eu:profile:biixx:ver1.0" +
                "#urn:www.difi.no:ehf:kreditnota:ver1" +
                "::2.0";
        String s2 = "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote" +
                "##urn:www.cenbii.eu:transaction:biicoretrdm014:ver1.0" +
                ":#urn:www.cenbii.eu:profile:biixx:ver1.0" +
                "#urn:www.difi.no:ehf:kreditnota:ver1" +
                "::3.0";

        PeppolDocumentTypeId d1 = PeppolDocumentTypeId.valueOf(s);
        PeppolDocumentTypeId d2 = PeppolDocumentTypeId.valueOf(s);

        PeppolDocumentTypeId d3 = PeppolDocumentTypeId.valueOf(s2);
        assertEquals(d1, d2);

        assertNotEquals(d1, d3);
    }

    /**
     * Verifies the Tender document
     */
    @Test
    public void tender() {
        PeppolDocumentTypeId tender = new PeppolDocumentTypeId(
                "urn:oasis:names:specification:ubl:schema:xsd:Tender-2",
                "Tender",
                new CustomizationIdentifier("urn:www.cenbii.eu:transaction:biitrdm090:ver3.0")
                , "2.1");
        assertEquals("urn:oasis:names:specification:ubl:schema:xsd:Tender-2::Tender" +
                "##urn:www.cenbii.eu:transaction:biitrdm090:ver3.0" +
                "::2.1", tender.toString());
    }

    @Test
    public void testLotsOfExpectedPeppolDocumentTypeIds() {

        // These are known to be in use and should be parsable without errors
        String[] documentIdentifiers = {
                "urn:www.peppol.eu:schema:xsd:VirtualCompanyDossier-1::VirtualCompanyDossier##urn:www.cenbii.eu:transaction:biicoretrdm991:ver0.1:#urn:www.peppol.eu:bis:peppol991a:ver1.0::0.1",
                "urn:www.peppol.eu:schema:xsd:VirtualCompanyDossierPackage-1::VirtualCompanyDossierPackage##urn:www.cenbii.eu:transaction:biicoretrdm992:ver0.1:#urn:www.peppol.eu:bis:peppol992a:ver1.0::0.1",
                "urn:www.peppol.eu:schema:xsd:CatalogueTemplate-1::CatalogueTemplate##urn:www.cenbii.eu:transaction:biicoretrdm993:ver0.1:#urn:www.peppol.eu:bis:peppol993a:ver1.0::0.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Catalogue-2::Catalogue##urn:www.cenbii.eu:transaction:biicoretrdm019:ver1.0:#urn:www.peppol.eu:bis:peppol1a:ver1.0::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:ApplicationResponse-2::ApplicationResponse##urn:www.cenbii.eu:transaction:biicoretrdm057:ver1.0:#urn:www.peppol.eu:bis:peppol1a:ver1.0::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:ApplicationResponse-2::ApplicationResponse##urn:www.cenbii.eu:transaction:biicoretrdm058:ver1.0:#urn:www.peppol.eu:bis:peppol1a:ver1.0::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:Catalogue-2::Catalogue##urn:www.cenbii.eu:transaction:biitrns019:ver2.0:extended:urn:www.peppol.eu:bis:peppol1a:ver4.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Order-2::Order##urn:www.cenbii.eu:transaction:biicoretrdm001:ver1.0:#urn:www.peppol.eu:bis:peppol3a:ver1.0::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:Order-2::Order##urn:www.cenbii.eu:transaction:biitrns001:ver2.0:extended:urn:www.peppol.eu:bis:peppol03a:ver2.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Order-2::Order##urn:www.cenbii.eu:transaction:biitrns001:ver2.0:extended:urn:www.peppol.eu:bis:peppol3a:ver2.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0:#urn:www.peppol.eu:bis:peppol4a:ver1.0::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biitrns010:ver2.0:extended:urn:www.peppol.eu:bis:peppol4a:ver2.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0:#urn:www.peppol.eu:bis:peppol5a:ver1.0::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:www.cenbii.eu:transaction:biicoretrdm014:ver1.0:#urn:www.peppol.eu:bis:peppol5a:ver1.0::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biicoretrdm015:ver1.0:#urn:www.peppol.eu:bis:peppol5a:ver1.0::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biitrns010:ver2.0:extended:urn:www.peppol.eu:bis:peppol5a:ver2.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:www.cenbii.eu:transaction:biitrns014:ver2.0:extended:urn:www.peppol.eu:bis:peppol5a:ver2.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Order-2::Order##urn:www.cenbii.eu:transaction:biicoretrdm001:ver1.0:#urn:www.peppol.eu:bis:peppol6a:ver1.0::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:OrderResponseSimple-2::OrderResponseSimple##urn:www.cenbii.eu:transaction:biicoretrdm002:ver1.0:#urn:www.peppol.eu:bis:peppol6a:ver1.0::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:OrderResponseSimple-2::OrderResponseSimple##urn:www.cenbii.eu:transaction:biicoretrdm003:ver1.0:#urn:www.peppol.eu:bis:peppol6a:ver1.0::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0:#urn:www.peppol.eu:bis:peppol6a:ver1.0::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:www.cenbii.eu:transaction:biicoretrdm014:ver1.0:#urn:www.peppol.eu:bis:peppol6a:ver1.0::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biicoretrdm015:ver1.0:#urn:www.peppol.eu:bis:peppol6a:ver1.0::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0:#urn:www.peppol.eu:bis:peppol4a:ver1.0#urn:www.difi.no:ehf:faktura:ver1::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:www.cenbii.eu:transaction:biicoretrdm014:ver1.0:#urn:www.cenbii.eu:profile:biixx:ver1.0#urn:www.difi.no:ehf:kreditnota:ver1::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:Order-2::Order##urn:www.cenbii.eu:transaction:biitrns001:ver2.0:extended:urn:www.peppol.eu:bis:peppol28a:ver1.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:OrderResponse-2::Order##urn:www.cenbii.eu:transaction:biitrns076:ver2.0:extended:urn:www.peppol.eu:bis:peppol28a:ver1.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:OrderResponse-2::OrderResponse##urn:www.cenbii.eu:transaction:biitrns076:ver2.0:extended:urn:www.peppol.eu:bis:peppol28a:ver1.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:DespatchAdvice-2::DespatchAdvice##urn:www.cenbii.eu:transaction:biitrns016:ver1.0:extended:urn:www.peppol.eu:bis:peppol30a:ver1.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:ApplicationResponse-2::ApplicationResponse##urn:www.cenbii.eu:transaction:biitrns071:ver2.0:extended:urn:www.peppol.eu:bis:peppol36a:ver1.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:cen.eu:en16931:2017#compliant#urn:fdc:peppol.eu:2017:poacc:billing:3.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:cen.eu:en16931:2017#compliant#urn:fdc:peppol.eu:2017:poacc:billing:3.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Order-2::Order##urn:www.cenbii.eu:transaction:biitrns001:ver2.0:extended:urn:www.peppol.eu:bis:peppol28a:ver1.0:extended:urn:fdc:peppol-authority.co.uk:spec:ordering:ver1.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:OrderResponse-2::Order##urn:www.cenbii.eu:transaction:biitrns076:ver2.0:extended:urn:www.peppol.eu:bis:peppol28a:ver1.0:extended:urn:fdc:peppol-authority.co.uk:spec:ordering:ver1.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:OrderResponse-2::OrderResponse##urn:www.cenbii.eu:transaction:biitrns076:ver2.0:extended:urn:www.peppol.eu:bis:peppol28a:ver1.0:extended:urn:fdc:peppol-authority.co.uk:spec:ordering:ver1.0::2.1",
                "urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100::CrossIndustryInvoice##urn:cen.eu:en16931:2017#compliant#urn:fdc:peppol.eu:2017:poacc:billing:3.0::D16B",
                "urn:oasis:names:specification:ubl:schema:xsd:ExpressionOfInterestRequest-2::ExpressionOfInterestRequest##urn:www.cenbii.eu:transaction:biitrdm081:ver3.0:extended:urn:fdc:peppol.eu:2017:pracc:t001:ver1.0::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:ExpressionOfInterestResponse-2::ExpressionOfInterestResponse##urn:www.cenbii.eu:transaction:biitrdm082:ver3.0:extended:urn:fdc:peppol.eu:2017:pracc:t002:ver1.0::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:TenderStatusRequest-2::TenderStatusRequest##urn:www.cenbii.eu:transaction:biitrdm097:ver3.0:extended:urn:fdc:peppol.eu:2017:pracc:t003:ver1.0::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:CallForTenders-2::CallForTenders##urn:www.cenbii.eu:transaction:biitrdm083:ver3.0:extended:urn:fdc:peppol.eu:2017:pracc:t004:ver1.0::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:TenderReceipt-2::TenderReceipt##urn:www.cenbii.eu:transaction:biitrdm045:ver3.0:extended:urn:fdc:peppol.eu:2017:pracc:t006:ver1.0::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:Tender-2::Tender##urn:www.cenbii.eu:transaction:biitrdm090:ver3.0:extended:urn:fdc:peppol.eu:2017:pracc:t005:ver1.0::2.2",
                "urn:oioubl:names:specification:oioubl:schema:xsd:UtilityStatement-2::UtilityStatement##OIOUBL-2.02::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:Reminder-2::Reminder##OIOUBL-2.02::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:cen.eu:en16931:2017#conformant#urn:UBL.BE:1.0.0.20180214::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:cen.eu:en16931:2017#conformant#urn:UBL.BE:1.0.0.20180214::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:ApplicationResponse-2::ApplicationResponse##urn:www.peppol.eu:transaction:biitrns111:ver1.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Catalogue-2::Catalogue##urn:fdc:peppol.eu:poacc:trns:catalogue:3::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:ApplicationResponse-2::ApplicationResponse##urn:fdc:peppol.eu:poacc:trns:catalogue_response:3::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Order-2::Order##urn:fdc:peppol.eu:poacc:trns:order:3::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:ApplicationResponse-2::ApplicationResponse##urn:fdc:peppol.eu:poacc:trns:invoice_response:3::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Catalogue-2::Catalogue##urn:fdc:peppol.eu:poacc:trns:punch_out:3::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:OrderResponse-2::OrderResponse##urn:fdc:peppol.eu:poacc:trns:order_response:3::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:DespatchAdvice-2::DespatchAdvice##urn:fdc:peppol.eu:poacc:trns:despatch_advice:3::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:OrderResponse-2::OrderResponse##urn:fdc:peppol.eu:poacc:trns:order_agreement:3::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:ApplicationResponse-2::ApplicationResponse##urn:fdc:peppol.eu:poacc:trns:mlr:3::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:cen.eu:en16931:2017#compliant#urn:fdc:nen.nl:nlcius:v1.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:cen.eu:en16931:2017#compliant#urn:fdc:nen.nl:nlcius:v1.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:cen.eu:en16931:2017#conformant#urn:fdc:peppol.eu:2017:poacc:billing:international:sg:3.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::CreditNote##urn:cen.eu:en16931:2017#conformant#urn:fdc:peppol.eu:2017:poacc:billing:international:sg:3.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:cen.eu:en16931:2017#conformant#urn:fdc:peppol.eu:2017:poacc:billing:international:sg:3.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:fdc:www.efaktura.gov.pl:ver1.0:trns:account_corr:ver1.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:cen.eu:en16931:2017#compliant#urn:fdc:peppol.eu:2017:poacc:billing:3.0#extended#urn:fdc:www.efaktura.gov.pl:ver1.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:ReceiptAdvice-2::ReceiptAdvice##urn:fdc:www.efaktura.gov.pl:ver1.0:trns:receipt_advice:ver1.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:cen.eu:en16931:2017#conformant#urn:fdc:peppol.eu:2017:poacc:billing:international:aunz:3.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:cen.eu:en16931:2017#conformant#urn:fdc:peppol.eu:2017:poacc:billing:international:aunz:3.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:cen.eu:en16931:2017#conformant#urn:fdc:peppol.eu:2017:poacc:selfbilling:international:aunz:3.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:cen.eu:en16931:2017#conformant#urn:fdc:peppol.eu:2017:poacc:selfbilling:international:aunz:3.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:cen.eu:en16931:2017#compliant#urn:xoev-de:kosit:standard:xrechnung_2.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:cen.eu:en16931:2017#compliant#urn:xoev-de:kosit:standard:xrechnung_2.0::2.1",
                "urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100::CrossIndustryInvoice##urn:cen.eu:en16931:2017#compliant#urn:xoev-de:kosit:standard:xrechnung_2.0::D16B",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:cen.eu:en16931:2017#compliant#urn:fdc:nen.nl:nlcius:v1.0#conformant#urn:fdc:nen.nl:gaccount:v1.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Order-2::Order##urn:fdc:peppol.eu:poacc:trns:order:3:extended:urn:fdc:anskaffelser.no:2019:ehf:spec:3.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:OrderChange-2::OrderChange##urn:fdc:anskaffelser.no:2019:ehf:spec:adv-order-change:3.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:OrderCancellation-2::OrderCancellation##urn:fdc:anskaffelser.no:2019:ehf:spec:adv-order-cancellation:3.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:OrderResponse-2::OrderResponse##urn:fdc:peppol.eu:poacc:trns:order_response:3:extended:urn:fdc:anskaffelser.no:2019:ehf:spec:3.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:cen.eu:en16931:2017::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:cen.eu:en16931:2017::2.1",
                "urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100::CrossIndustryInvoice##urn:cen.eu:en16931:2017::D16B",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:cen.eu:en16931:2017#compliant#urn:xoev-de:kosit:standard:xrechnung_2.0#conformant#urn:xoev-de:kosit:extension:xrechnung_2.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:cen.eu:en16931:2017#compliant#urn:xoev-de:kosit:standard:xrechnung_2.0#conformant#urn:xoev-de:kosit:extension:xrechnung_2.0::2.1",
                "urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100::CrossIndustryInvoice##urn:cen.eu:en16931:2017#compliant#urn:xoev-de:kosit:standard:xrechnung_2.0#conformant#urn:xoev-de:kosit:extension:xrechnung_2.0::D16B",
                "urn:oasis:names:specification:ubl:schema:xsd:Order-2::Order##urn:fdc:peppol.eu:poacc:trns:order:3:restrictive:urn:www.agid.gov.it:trns:ordine:3.1::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:OrderResponse-2::OrderResponse##urn:fdc:peppol.eu:poacc:trns:order_response:3:restrictive:urn:www.agid.gov.it:trns:risposta_ordine:3.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:DespatchAdvice-2::DespatchAdvice##urn:fdc:peppol.eu:poacc:trns:despatch_advice:3:extended:urn:www.agid.gov.it:trns:ddt:3.1::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Order-2::Order##urn:fdc:peppol.eu:poacc:trns:order:3:extended:urn:fdc:anskaffelser.no:2019:ehf:spec:3.0::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:OrderChange-2::OrderChange##urn:fdc:anskaffelser.no:2019:ehf:spec:adv-order-change:3.0::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:OrderCancellation-2::OrderCancellation##urn:fdc:anskaffelser.no:2019:ehf:spec:adv-order-cancellation:3.0::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:OrderResponse-2::OrderResponse##urn:fdc:peppol.eu:poacc:trns:order_response:3:extended:urn:fdc:anskaffelser.no:2019:ehf:spec:3.0::2.2",
                "urn:iso:std:iso:20022:tech:xsd:pain.001.001.03::Document##urn:fdc:bits.no:2017:iso20022:1.5::03",
                "urn:fdc:difi.no:2017:payment:extras-1::ReceptionAcknowledgement##urn:fdc:difi.no:2017:payment:handling:1.0:for:urn:iso:std:iso:20022:tech:xsd:pain.002.001.03:restricted:urn:fdc:bits.no:2017:iso20022:1.5::1.0",
                "urn:fdc:difi.no:2017:payment:extras-1::HandlingException##urn:fdc:difi.no:2017:payment:handling:1.0:for:urn:iso:std:iso:20022:tech:xsd:pain.002.001.03:restricted:urn:fdc:bits.no:2017:iso20022:1.5::1.0",
                "urn:fdc:difi.no:2017:payment:extras-1::ReceptionAcknowledgement##urn:fdc:difi.no:2017:payment:handling:1.0:for:urn:iso:std:iso:20022:tech:xsd:camt.054.001.02:restricted:urn:fdc:bits.no:2017:iso20022:1.5::1.0",
                "urn:fdc:difi.no:2017:payment:extras-1::HandlingException##urn:fdc:difi.no:2017:payment:handling:1.0:for:urn:iso:std:iso:20022:tech:xsd:camt.054.001.02:restricted:urn:fdc:bits.no:2017:iso20022:1.5::1.0",
                "urn:fdc:difi.no:2017:payment:extras-1::ReceptionAcknowledgement##urn:fdc:difi.no:2017:payment:handling:1.0:for:urn:iso:std:iso:20022:tech:xsd:pain.001.001.03:restricted:urn:fdc:bits.no:2017:iso20022:1.5::1.0",
                "urn:fdc:difi.no:2017:payment:extras-1::HandlingException##urn:fdc:difi.no:2017:payment:handling:1.0:for:urn:iso:std:iso:20022:tech:xsd:pain.001.001.03:restricted:urn:fdc:bits.no:2017:iso20022:1.5::1.0",
                "urn:iso:std:iso:20022:tech:xsd:pain.002.001.03::Document##urn:fdc:bits.no:2017:iso20022:1.5::03",
                "urn:iso:std:iso:20022:tech:xsd:camt.054.001.02::Document##urn:fdc:bits.no:2017:iso20022:1.5::02",
                "urn:iso:std:iso:20022:tech:xsd:camt.055.001.01::Document##urn:fdc:bits.no:2017:iso20022:1.5::01",
                "urn:fdc:difi.no:2017:payment:extras-1::ReceptionAcknowledgement##urn:fdc:difi.no:2017:payment:handling:1.0:for:urn:iso:std:iso:20022:tech:xsd:camt.029.001.03:restricted:urn:fdc:bits.no:2017:iso20022:1.5::1.0",
                "urn:fdc:difi.no:2017:payment:extras-1::HandlingException##urn:fdc:difi.no:2017:payment:handling:1.0:for:urn:iso:std:iso:20022:tech:xsd:camt.029.001.03:restricted:urn:fdc:bits.no:2017:iso20022:1.5::1.0",
                "urn:fdc:difi.no:2017:payment:extras-1::ReceptionAcknowledgement##urn:fdc:difi.no:2017:payment:handling:1.0:for:urn:iso:std:iso:20022:tech:xsd:camt.055.001.01:restricted:urn:fdc:bits.no:2017:iso20022:1.5::1.0",
                "urn:fdc:difi.no:2017:payment:extras-1::HandlingException##urn:fdc:difi.no:2017:payment:handling:1.0:for:urn:iso:std:iso:20022:tech:xsd:camt.055.001.01:restricted:urn:fdc:bits.no:2017:iso20022:1.5::1.0",
                "urn:iso:std:iso:20022:tech:xsd:camt.029.001.03::Document##urn:fdc:bits.no:2017:iso20022:1.5::03",
                "urn:fdc:difi.no:2017:payment:extras-1::ReceptionAcknowledgement##urn:fdc:difi.no:2017:payment:handling:1.0:for:urn:urn:iso:std:iso:20022:tech:xsd:camt.054.001.02:restricted:urn:fdc:bits.no:2017:iso20022:1.5::1.0",
                "urn:iso:std:iso:20022:tech:xsd:camt.053.001.02::Document##urn:fdc:bits.no:2017:iso20022:1.5::02",
                "urn:fdc:difi.no:2017:payment:extras-1::ReceptionAcknowledgement##urn:fdc:difi.no:2017:payment:handling:1.0:for:urn:iso:std:iso:20022:tech:xsd:camt.053.001.02:restricted:urn:fdc:bits.no:2017:iso20022:1.5::1.0",
                "urn:fdc:difi.no:2017:payment:extras-1::HandlingException##urn:fdc:difi.no:2017:payment:handling:1.0:for:urn:iso:std:iso:20022:tech:xsd:camt.053.001.02:restricted:urn:fdc:bits.no:2017:iso20022:1.5::1.0",
                "urn:iso:std:iso:20022:tech:xsd:camt.052.001.02::Document##urn:fdc:bits.no:2017:iso20022:1.5::02",
                "urn:fdc:difi.no:2017:payment:extras-1::ReceptionAcknowledgement##urn:fdc:difi.no:2017:payment:handling:1.0:for:urn:iso:std:iso:20022:tech:xsd:camt.052.001.02:restricted:urn:fdc:bits.no:2017:iso20022:1.5::1.0",
                "urn:fdc:difi.no:2017:payment:extras-1::HandlingException##urn:fdc:difi.no:2017:payment:handling:1.0:for:urn:iso:std:iso:20022:tech:xsd:camt.052.001.02:restricted:urn:fdc:bits.no:2017:iso20022:1.5::1.0",
                "urn:oasis:names:specification:ubl:schema:xsd:Enquiry-2::Enquiry##urn:fdc:peppol.eu:prac:trns:t007:1.0::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:EnquiryResponse-2::EnquiryResponse##urn:fdc:peppol.eu:prac:trns:t008:1.0::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:Enquiry-2::Enquiry##urn:fdc:peppol.eu:prac:trns:t009:1.0::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:EnquiryResponse-2::EnquiryResponse##urn:fdc:peppol.eu:prac:trns:t010:1.0::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:Catalogue-2::Catalogue##urn:www.cenbii.eu:transaction:biitrns019:ver2.0:extended:urn:www.peppol.eu:bis:peppol1a:ver2.0:extended:urn:www.difi.no:ehf:katalog:ver1.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:ApplicationResponse-2::ApplicationResponse##urn:www.cenbii.eu:transaction:biitrns058:ver2.0:extended:urn:www.difi.no:ehf:katalogbekreftelse:ver1.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Order-2::Order##urn:www.cenbii.eu:transaction:biitrns001:ver2.0:extended:urn:www.peppol.eu:bis:peppol28a:ver1.0:extended:urn:www.difi.no:ehf:ordre:ver1.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:OrderResponse-2::OrderResponse##urn:www.cenbii.eu:transaction:biitrns076:ver2.0:extended:urn:www.peppol.eu:bis:peppol28a:ver1.0:extended:urn:www.difi.no:ehf:ordrebekreftelse:ver1.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:DespatchAdvice-2::DespatchAdvice##urn:www.cenbii.eu:transaction:biitrns016:ver1.0:extended:urn:www.peppol.eu:bis:peppol30a:ver1.0:extended:urn:www.difi.no:ehf:pakkseddel:ver1.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Reminder-2::Reminder##urn:www.cenbii.eu:transaction:biicoretrdm017:ver1.0:#urn:www.cenbii.eu:profile:biixy:ver1.0#urn:www.difi.no:ehf:purring:ver1::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:Catalogue-2::Catalogue##urn:fdc:peppol.eu:poacc:trns:catalogue:3:extended:urn:fdc:anskaffelser.no:2019:ehf:spec:3.0::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:ApplicationResponse-2::ApplicationResponse##urn:fdc:peppol.eu:poacc:trns:catalogue_response:3:extended:urn:fdc:anskaffelser.no:2019:ehf:spec:3.0::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:OrderResponse-2::OrderResponse##urn:fdc:peppol.eu:poacc:trns:order_agreement:3:extended:urn:fdc:anskaffelser.no:2019:ehf:spec:3.0::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:DespatchAdvice-2::DespatchAdvice##urn:fdc:peppol.eu:poacc:trns:despatch_advice:3:extended:urn:fdc:anskaffelser.no:2019:ehf:spec:3.0::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:cen.eu:en16931:2017#compliant#urn:fdc:peppol.eu:2017:poacc:billing:3.0#conformant#urn:fdc:anskaffelser.no:2019:ehf:reminder:3.0::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:fdc:anskaffelser.no:2019:ehf:spec:payment-request:3.0::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:cen.eu:en16931:2017#compliant#urn:fdc:peppol.eu:2017:poacc:billing:3.0#conformant#urn:fdc:anskaffelser.no:2019:ehf:forward-billing:3.0::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:cen.eu:en16931:2017#compliant#urn:fdc:peppol.eu:2017:poacc:billing:3.0#conformant#urn:fdc:anskaffelser.no:2019:ehf:forward-billing:3.0::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:cen.eu:en16931:2017#compliant#urn:xoev-de:kosit:standard:xrechnung_2.1::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:cen.eu:en16931:2017#compliant#urn:xoev-de:kosit:standard:xrechnung_2.1::2.1",
                "urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100::CrossIndustryInvoice##urn:cen.eu:en16931:2017#compliant#urn:xoev-de:kosit:standard:xrechnung_2.1::D16B",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:cen.eu:en16931:2017#compliant#urn:xoev-de:kosit:standard:xrechnung_2.1#conformant#urn:xoev-de:kosit:extension:xrechnung_2.1::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:cen.eu:en16931:2017#compliant#urn:xoev-de:kosit:standard:xrechnung_2.1#conformant#urn:xoev-de:kosit:extension:xrechnung_2.1::2.1",
                "urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100::CrossIndustryInvoice##urn:cen.eu:en16931:2017#compliant#urn:xoev-de:kosit:standard:xrechnung_2.1#conformant#urn:xoev-de:kosit:extension:xrechnung_2.1::D16B",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:cen.eu:en16931:2017#compliant#urn:fdc:peppol.eu:2017:poacc:billing:3.0#extended#urn:fdc:www.efaktura.gov.pl:ver2.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:cen.eu:en16931:2017#compliant#urn:fdc:peppol.eu:2017:poacc:billing:3.0#extended#urn:fdc:www.efaktura.gov.pl:ver2.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:SelfBilledCreditNote-2::SelfBilledCreditNote##urn:cen.eu:en16931:2017#compliant#urn:fdc:peppol.eu:2017:poacc:billing:3.0#extended#urn:fdc:www.efaktura.gov.pl:ver2.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:ExpressionOfInterestRequest-2::ExpressionOfInterestRequest##urn:fdc:peppol.eu:prac:trns:t001:1.1::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:ExpressionOfInterestResponse-2::ExpressionOfInterestResponse##urn:fdc:peppol.eu:prac:trns:t002:1.1::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:TenderStatusRequest-2::TenderStatusRequest##urn:fdc:peppol.eu:prac:trns:t003:1.1::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:CallForTenders-2::CallForTenders##urn:fdc:peppol.eu:prac:trns:t004:1.1::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:Tender-2::Tender##urn:fdc:peppol.eu:prac:trns:t005:1.1::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:TenderReceipt-2::TenderReceipt##urn:fdc:peppol.eu:prac:trns:t006:1.1::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:Enquiry-2::Enquiry##urn:fdc:peppol.eu:prac:trns:t007:1.1::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:EnquiryResponse-2::EnquiryResponse##urn:fdc:peppol.eu:prac:trns:t008:1.1::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:Enquiry-2::Enquiry##urn:fdc:peppol.eu:prac:trns:t009:1.1::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:EnquiryResponse-2::EnquiryResponse##urn:fdc:peppol.eu:prac:trns:t010:1.1::2.2",
                "urn:oasis:names:tc:ebxml-regrep:xsd:query:4.0::QueryRequest##urn:fdc:peppol.eu:prac:trns:t011:1.1::4.0",
                "urn:oasis:names:tc:ebxml-regrep:xsd:query:4.0::QueryResponse##urn:fdc:peppol.eu:prac:trns:t012:1.1::4.0",
                "urn:oasis:names:specification:ubl:schema:xsd:TenderWithdrawal-2::TenderWithdrawal##urn:fdc:peppol.eu:prac:trns:t013:1.1::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:TenderReceipt-2::TenderReceipt##urn:fdc:peppol.eu:prac:trns:t014:1.1::2.2",
                "urn:oasis:names:tc:ebxml-regrep:xsd:lcm:4.0::SubmitObjectsRequest##urn:fdc:peppol.eu:prac:trns:t015:1.1::4.0",
                "urn:oasis:names:specification:ubl:schema:xsd:ApplicationResponse-2::ApplicationResponse##urn:fdc:peppol.eu:prac:trns:t016:1.1::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:AwardedNotification-2::AwardedNotification##urn:fdc:peppol.eu:prac:trns:t017:1.4::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:AwardedNotification-2::AwardedNotification##urn:fdc:peppol.eu:prac:trns:t017:1.1::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:cen.eu:en16931:2017#compliant#urn:xoev-de:kosit:standard:xrechnung_2.2::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:cen.eu:en16931:2017#compliant#urn:xoev-de:kosit:standard:xrechnung_2.2::2.1",
                "urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100::CrossIndustryInvoice##urn:cen.eu:en16931:2017#compliant#urn:xoev-de:kosit:standard:xrechnung_2.2::D16B",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:cen.eu:en16931:2017#compliant#urn:xoev-de:kosit:standard:xrechnung_2.2#conformant#urn:xoev-de:kosit:extension:xrechnung_2.2::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:cen.eu:en16931:2017#compliant#urn:xoev-de:kosit:standard:xrechnung_2.2#conformant#urn:xoev-de:kosit:extension:xrechnung_2.2::2.1",
                "urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100::CrossIndustryInvoice##urn:cen.eu:en16931:2017#compliant#urn:xoev-de:kosit:standard:xrechnung_2.2#conformant#urn:xoev-de:kosit:extension:xrechnung_2.2::D16B",
                "urn:oasis:names:specification:ubl:schema:xsd:DespatchAdvice-2::DespatchAdvice##urn:fdc:peppol.eu:logistics:trns:advanced_despatch_advice:1::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:ApplicationResponse-2::ApplicationResponse##urn:fdc:peppol.eu:logistics:trns:despatch_advice_response:1::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:WeightStatement-2::WeightStatement##urn:fdc:peppol.eu:logistics:trns:weight_statement:1::2.3",
                "urn:oasis:names:specification:ubl:schema:xsd:UtilityStatement-2::UtilityStatement##urn:fdc:www.efaktura.gov.pl:ver2.0:trns:us:ver1.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:cen.eu:en16931:2017#compliant#urn:fdc:nen.nl:nlcius:v1.0#compliant#urn:fdc:setu.nl:invoice:v2.2::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:OrderChange-2::OrderChange##urn:fdc:peppol.eu:poacc:trns:order_change:3::2.3",
                "urn:oasis:names:specification:ubl:schema:xsd:OrderCancellation-2::OrderCancellation##urn:fdc:peppol.eu:poacc:trns:order_cancellation:3::2.3",
                "urn:oasis:names:specification:ubl:schema:xsd:OrderResponse-2::OrderResponse##urn:fdc:peppol.eu:poacc:trns:order_response_advanced:3::2.3",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:cen.eu:en16931:2017#compliant#urn:xoev-de:kosit:standard:xrechnung_2.3::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:cen.eu:en16931:2017#compliant#urn:xoev-de:kosit:standard:xrechnung_2.3::2.1",
                "urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100::CrossIndustryInvoice##urn:cen.eu:en16931:2017#compliant#urn:xoev-de:kosit:standard:xrechnung_2.3::D16B",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:cen.eu:en16931:2017#compliant#urn:xoev-de:kosit:standard:xrechnung_2.3#conformant#urn:xoev-de:kosit:extension:xrechnung_2.3::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:cen.eu:en16931:2017#compliant#urn:xoev-de:kosit:standard:xrechnung_2.3#conformant#urn:xoev-de:kosit:extension:xrechnung_2.3::2.1",
                "urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100::CrossIndustryInvoice##urn:cen.eu:en16931:2017#compliant#urn:xoev-de:kosit:standard:xrechnung_2.3#conformant#urn:xoev-de:kosit:extension:xrechnung_2.3::D16B",
                "urn:oasis:names:specification:ubl:schema:xsd:ExpressionOfInterestRequest-2::ExpressionOfInterestRequest##urn:fdc:peppol.eu:prac:trns:t001:1.2::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:ExpressionOfInterestResponse-2::ExpressionOfInterestResponse##urn:fdc:peppol.eu:prac:trns:t002:1.2::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:ExpressionOfInterestRequest-2::ExpressionOfInterestRequest##urn:fdc:peppol.eu:prac:trns:t021:1.2::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:ExpressionOfInterestResponse-2::ExpressionOfInterestResponse##urn:fdc:peppol.eu:prac:trns:t022:1.2::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:TenderStatusRequest-2::TenderStatusRequest##urn:fdc:peppol.eu:prac:trns:t003:1.2::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:CallForTenders-2::CallForTenders##urn:fdc:peppol.eu:prac:trns:t004:1.2::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:Tender-2::Tender##urn:fdc:peppol.eu:prac:trns:t005:1.2::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:TenderReceipt-2::TenderReceipt##urn:fdc:peppol.eu:prac:trns:t006:1.2::2.2",
                "urn:oasis:names:tc:ebxml-regrep:xsd:query:4.0::QueryRequest##urn:fdc:peppol.eu:prac:trns:t011:1.2::4.0",
                "urn:oasis:names:tc:ebxml-regrep:xsd:query:4.0::QueryResponse##urn:fdc:peppol.eu:prac:trns:t012:1.2::4.0",
                "urn:oasis:names:tc:ebxml-regrep:xsd:lcm:4.0::SubmitObjectsRequest##urn:fdc:peppol.eu:prac:trns:t015:1.2::4.0",
                "urn:oasis:names:specification:ubl:schema:xsd:ApplicationResponse-2::ApplicationResponse##urn:fdc:peppol.eu:prac:trns:t016:1.2::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:ApplicationResponse-2::ApplicationResponse##urn:fdc:peppol.eu:prac:trns:t018:1.1::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:Qualification-2::Qualification##urn:fdc:peppol.eu:prac:trns:t019:1.1::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:TenderReceipt-2::TenderReceipt##urn:fdc:peppol.eu:prac:trns:t020:1.1::2.2",
                "urn:oasis:names:specification:ubl:schema:xsd:TransportExecutionPlanRequest-2::TransportExecutionPlanRequest##urn:fdc:peppol.eu:logistics:trns:transport_execution_plan_request:1::2.3",
                "urn:oasis:names:specification:ubl:schema:xsd:TransportExecutionPlan-2::TransportExecutionPlan##urn:fdc:peppol.eu:logistics:trns:transport_execution_plan:1::2.3",
                "urn:oasis:names:specification:ubl:schema:xsd:Waybill-2::Waybill##urn:fdc:peppol.eu:logistics:trns:waybill:1::2.3",
                "urn:oasis:names:specification:ubl:schema:xsd:TransportationStatusRequest-2::TransportationStatusRequest##urn:fdc:peppol.eu:logistics:trns:transportation_status_request:1::2.3",
                "urn:oasis:names:specification:ubl:schema:xsd:TransportationStatus-2::TransportationStatus##urn:fdc:peppol.eu:logistics:trns:transportation_status:1::2.3",
                "urn:oasis:names:specification:ubl:schema:xsd:ReceiptAdvice-2::ReceiptAdvice##urn:fdc:peppol.eu:logistics:trns:receipt_advice:1::2.3",
                "http://ns.hr-xml.org/2007-04-15::TimeCard##hr-xml@nl-1.4::2.5",
                "urn:fdc:peppol:end-user-statistics-report:1.1::EndUserStatisticsReport##urn:fdc:peppol.eu:edec:trns:end-user-statistics-report:1.1::1.1",
                "urn:fdc:peppol:transaction-statistics-report:1.0::TransactionStatisticsReport##urn:fdc:peppol.eu:edec:trns:transaction-statistics-reporting:1.0::1.0",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:peppol:pint:billing-1::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:peppol:pint:billing-1::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:peppol:pint:billing-1@jp-1::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:peppol:pint:selfbilling-1@jp-1::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:peppol:pint:nontaxinvoice-1@jp-1::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:peppol:pint:billing-1@aunz-1::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:peppol:pint:billing-1@aunz-1::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:peppol:pint:selfbilling-1@aunz-1::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:peppol:pint:selfbilling-1@aunz-1::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:cen.eu:en16931:2017#compliant#urn:xeinkauf.de:kosit:xrechnung_3.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:cen.eu:en16931:2017#compliant#urn:xeinkauf.de:kosit:xrechnung_3.0::2.1",
                "urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100::CrossIndustryInvoice##urn:cen.eu:en16931:2017#compliant#urn:xeinkauf.de:kosit:xrechnung_3.0::D16B",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:cen.eu:en16931:2017#compliant#urn:xeinkauf.de:kosit:xrechnung_3.0#conformant#urn:xeinkauf.de:kosit:extension:xrechnung_3.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:cen.eu:en16931:2017#compliant#urn:xeinkauf.de:kosit:xrechnung_3.0#conformant#urn:xeinkauf.de:kosit:extension:xrechnung_3.0::2.1",
                "urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100::CrossIndustryInvoice##urn:cen.eu:en16931:2017#compliant#urn:xeinkauf.de:kosit:xrechnung_3.0#conformant#urn:xeinkauf.de:kosit:extension:xrechnung_3.0::D16B",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:peppol:pint:billing-1@sg-1::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:peppol:pint:billing-1@sg-1::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:peppol:pint:billing-1@my-1::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:peppol:pint:billing-1@my-1::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:peppol:pint:selfbilling-1@my-1::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:peppol:pint:selfbilling-1@my-1::2.1",
                "http://ns.hr-xml.org/2007-04-15::StaffingOrder##hr-xml@nl-1.4::2.5",
                "http://ns.hr-xml.org/2007-04-15::StaffingOrder##hr-xml:status@nl-1.4::2.5",
                "http://ns.hr-xml.org/2007-04-15::HumanResource##hr-xml@nl-1.4::2.5",
                "http://ns.hr-xml.org/2007-04-15::HumanResource##hr-xml:status@nl-1.4::2.5",
                "http://ns.hr-xml.org/2007-04-15::Assignment##hr-xml@nl-1.4.1::2.5",
                "http://ns.hr-xml.org/2007-04-15::Assignment##hr-xml:status@nl-1.4::2.5",
                "urn:fdc:digdir.no:2020:innbyggerpost:xsd::innbyggerpost##urn:fdc:digdir.no:2020:innbyggerpost:schema:digital::1.0",
                "urn:fdc:digdir.no:2020:innbyggerpost:xsd::innbyggerpost##urn:fdc:digdir.no:2020:innbyggerpost:schema:utskrift::1.0",
                "urn:fdc:digdir.no:2020:innbyggerpost:xsd::innbyggerpost##urn:fdc:digdir.no:2020:innbyggerpost:schema:flyttet::1.0",
                "urn:fdc:digdir.no:2020:innbyggerpost:xsd::innbyggerpost##urn:fdc:digdir.no:2020:innbyggerpost:schema:leveringskvittering::1.0",
                "urn:fdc:digdir.no:2020:innbyggerpost:xsd::innbyggerpost##urn:fdc:digdir.no:2020:innbyggerpost:schema:feil::1.0",
                "urn:fdc:digdir.no:2020:innbyggerpost:xsd::innbyggerpost##urn:fdc:digdir.no:2020:innbyggerpost:schema:aapningskvittering::1.0",
                "urn:fdc:digdir.no:2020:innbyggerpost:xsd::innbyggerpost##urn:fdc:digdir.no:2020:innbyggerpost:schema:mottakskvittering::1.0",
                "urn:fdc:digdir.no:2020:innbyggerpost:xsd::innbyggerpost##urn:fdc:digdir.no:2020:innbyggerpost:schema:varslingfeiletkvittering::1.0",
                "urn:fdc:digdir.no:2020:innbyggerpost:xsd::innbyggerpost##urn:fdc:digdir.no:2020:innbyggerpost:schema:returpostkvittering::1.0",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:cen.eu:en16931:2017#compliant#urn:fdc:peppol.eu:2017:poacc:selfbilling:3.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:cen.eu:en16931:2017#compliant#urn:fdc:peppol.eu:2017:poacc:selfbilling:3.0::2.1",
                "http://www.ketenstandaard.nl/onderhoudsopdracht/SALES/005::MaintenanceInstruction##dico:maintenanceinstruction@nl-1.0::1.0",
                "urn:peppol:doctype:pdf+xml##urn:cen.eu:en16931:2017#conformant#urn:peppol:france:billing:Factur-X:1.0::D22B",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:cen.eu:en16931:2017#compliant#urn:peppol:france:billing:cius:1.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:cen.eu:en16931:2017#conformant#urn:peppol:france:billing:extended:1.0::2.1",
                "urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100::CrossIndustryInvoice##urn:cen.eu:en16931:2017#compliant#urn:peppol:france:billing:cius:1.0::D22B",
                "urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100::CrossIndustryInvoice##urn:cen.eu:en16931:2017#conformant#urn:peppol:france:billing:extended:1.0::D22B",
                "urn:un:unece:uncefact:data:standard:CrossDomainAcknowledgementAndResponse:100::CrossDomainAcknowledgementAndResponse##urn:peppol:france:billing:cdv:1.0::D22B",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:cen.eu:en16931:2017#compliant#urn:peppol:france:billing:cius:1.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:cen.eu:en16931:2017#conformant#urn:peppol:france:billing:extended:1.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:peppol:pint:billing-1@ae-1::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:peppol:pint:billing-1@ae-1::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:peppol:pint:selfbilling-1@ae-1::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:peppol:pint:selfbilling-1@ae-1::2.1",
                "urn:fdc:peppol:tax-data-document:1.0::TaxData##urn:peppol:pint:taxdata-1@ae-1::1.0",
                "urn:fdc:peppol:tax-data-status:1.0::TaxDataStatus##urn:peppol:pint:taxdatastatus-1@ae-1::1.0",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:cen.eu:en16931:2017#conformant#urn:peppol:eb2b:1.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:cen.eu:en16931:2017#conformant#urn:peppol:eb2b:1.0::2.1",
                "urn:peppol:doctype:edifact:ORDERS##eb2b::0",
                "urn:peppol:doctype:edifact:ORDRSP##eb2b::0",
                "urn:peppol:doctype:edifact:DESADV##eb2b::0",
                "urn:peppol:doctype:edifact:INVOIC##eb2b::0",
                "urn:peppol:doctype:x12:850##eb2b::0",
                "urn:peppol:doctype:x12:855##eb2b::0",
                "urn:peppol:doctype:x12:856##eb2b::0",
                "urn:peppol:doctype:x12:810##eb2b::0",
                "urn:peppol:doctype:pdf+xml##eb2b:order-x:1.0::0",
                "urn:peppol:doctype:pdf+xml##eb2b:factur-x:1.0::0",
                "urn:peppol:doctype:bilateral##eb2b:order::0",
                "urn:peppol:doctype:bilateral##eb2b:order_response::0",
                "urn:peppol:doctype:bilateral##eb2b:despatch_advice::0",
                "urn:peppol:doctype:bilateral##eb2b:invoice::0",
                "urn:peppol:doctype:bilateral##eb2b:bilateral::0",
                "http://www.ketenstandaard.nl/onderhoudsstatus/SALES/005::MaintenanceStatus##dico:maintenancestatus@nl-1.0::1.0",
                "urn:oasis:names:specification:ubl:schema:xsd:ApplicationResponse-2::ApplicationResponse##urn:peppol:edec:mls:1.0::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Order-2::Order##urn:fdc:imda.gov.sg:trns:order_balance:1::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:peppol:pint:billing-1@eu-1::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:peppol:pint:billing-1@eu-1::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:ApplicationResponse-2::ApplicationResponse##urn:fdc:peppol.eu:logistics:trns:application_response:1::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:TransportExecutionPlanRequest-2::TransportExecutionPlanRequest##urn:fdc:peppol.eu:logistics:trns:transport_execution_plan_request:1::2.4",
                "urn:oasis:names:specification:ubl:schema:xsd:TransportExecutionPlan-2::TransportExecutionPlan##urn:fdc:peppol.eu:logistics:trns:transport_execution_plan:1::2.4",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:peppol:pint:billing-1@ng-1::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:peppol:pint:billing-1@ng-1::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:peppol:pint:selfbilling-1@ng-1::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:peppol:pint:selfbilling-1@ng-1::2.1",
                "urn:peppol:schema:taxdata:1.0::TaxData##urn:peppol:taxdata:ae-1::1.0",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:peppol:pint:billing-1@om-1::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:peppol:pint:billing-1@om-1::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:peppol:pint:selfbilling-1@om-1::2.1",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:peppol:pint:selfbilling-1@om-1::2.1",
                "urn:peppol:schema:om-taxdata:1.0::TaxData##urn:peppol:taxdata:om-1::1.0",
                "urn:peppol:schema:sk-taxdata:1.0::TaxData##urn:peppol:taxdata:sk-1::1.0",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:cen.eu:en16931:2017#compliant#urn:fdc:nen.nl:nlcius:v1.0#conformant#urn:fdc:energie-efactuur.nl:invoice:v3.0::2.1",
        };

        for (String s : documentIdentifiers) {
            PeppolDocumentTypeId d = PeppolDocumentTypeId.valueOf(s);
            assertEquals(d.toString(), s);
        }
    }
}
