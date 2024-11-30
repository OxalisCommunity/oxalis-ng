package network.oxalis.ng.as4.util;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class AS4ErrorCodeTest {

    @Test
    public void nameOf_validNames_Test(){

        assertEquals(AS4ErrorCode.nameOf("EBMS:0303"), AS4ErrorCode.EBMS_0303);
        assertEquals(AS4ErrorCode.nameOf("EBMS:0004"), AS4ErrorCode.EBMS_0004);

        assertEquals(AS4ErrorCode.nameOf("ebms:0303"), AS4ErrorCode.EBMS_0303);
        assertEquals(AS4ErrorCode.nameOf("ebms:0004"), AS4ErrorCode.EBMS_0004);
    }

    @Test
    public void nameOf_invalidNames_Test(){

        assertNull(AS4ErrorCode.nameOf("EBMS:9999"));
        assertNull(AS4ErrorCode.nameOf("ebms:9999"));
    }

    @Test
    public void toString_Test(){

        assertEquals(AS4ErrorCode.EBMS_0004.toString(), "EBMS:0004");
        assertEquals(AS4ErrorCode.EBMS_0303.toString(), "EBMS:0303");
    }

    @Test
    public void toString_and_getErrorCode_equality_Test(){

        assertEquals(AS4ErrorCode.EBMS_0004.toString(), AS4ErrorCode.EBMS_0004.getErrorCode());
        assertEquals(AS4ErrorCode.EBMS_0303.toString(), AS4ErrorCode.EBMS_0303.getErrorCode());
    }

}