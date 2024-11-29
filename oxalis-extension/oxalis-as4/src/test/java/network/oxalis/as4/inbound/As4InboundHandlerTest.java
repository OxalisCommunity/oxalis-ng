package network.oxalis.as4.inbound;

import network.oxalis.as4.lang.OxalisAs4Exception;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.PartInfo;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.PartProperties;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.PayloadInfo;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Property;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class As4InboundHandlerTest {

    @Test()
    public void testValidateMessageId_withValidHref() throws Exception{

        Property property = new Property();
        property.setName("MimeType");
        property.setValue("Dummy");

        PartProperties partProperties = new PartProperties();
        partProperties.getProperty().add(property);

        PartInfo partInfo = new PartInfo();
        partInfo.setHref("cid:attachedPayload");
        partInfo.setPartProperties(partProperties);

        PayloadInfo payloadInfo = new PayloadInfo();
        payloadInfo.getPartInfo().add(partInfo);

        As4InboundHandler.validatePayloads(payloadInfo);
    }

    @Test( expectedExceptions = {OxalisAs4Exception.class} )
    public void testValidateMessageId_withInvalidHref() throws Exception{
        PartInfo partInfo = new PartInfo();
        partInfo.setHref("http://difi.no");

        PayloadInfo payloadInfo = new PayloadInfo();
        payloadInfo.getPartInfo().add(partInfo);

        As4InboundHandler.validatePayloads(payloadInfo);

        fail();
    }


}