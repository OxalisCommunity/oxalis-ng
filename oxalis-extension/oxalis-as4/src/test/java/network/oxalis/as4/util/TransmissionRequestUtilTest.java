package network.oxalis.as4.util;

import network.oxalis.as4.common.As4MessageProperty;
import network.oxalis.vefa.peppol.common.model.DocumentTypeIdentifier;
import network.oxalis.vefa.peppol.common.model.ParticipantIdentifier;
import network.oxalis.vefa.peppol.common.model.Scheme;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TransmissionRequestUtilTest {

    private static String PROVIDED_VALUE = "provided value";
    private static String PROVIDED_SCHEME = "provided scheme";

    @Test
    public void testTranslateDocumentTypeToAction_defaultScheme() {
        String result = TransmissionRequestUtil.translateDocumentTypeToAction(DocumentTypeIdentifier.of(PROVIDED_VALUE));
        Assert.assertEquals(DocumentTypeIdentifier.DEFAULT_SCHEME + "::" + PROVIDED_VALUE, result);
    }

    @Test
    public void testTranslateDocumentTypeToAction_nullScheme() {
        String result = TransmissionRequestUtil.translateDocumentTypeToAction(DocumentTypeIdentifier.of(PROVIDED_VALUE, null));
        Assert.assertEquals(PROVIDED_VALUE, result);
    }

    @Test
    public void testTranslateDocumentTypeToAction_providedScheme() {
        String result = TransmissionRequestUtil.translateDocumentTypeToAction(DocumentTypeIdentifier.of(PROVIDED_VALUE, Scheme.of(PROVIDED_SCHEME)));
        Assert.assertEquals(PROVIDED_SCHEME + "::" + PROVIDED_VALUE, result);
    }


    @Test
    public void testTranslateParticipantIdentifierToRecipient_defaultScheme() {
        As4MessageProperty result = TransmissionRequestUtil.toAs4MessageProperty("hello", ParticipantIdentifier.of(PROVIDED_VALUE));

        Assert.assertEquals("hello", result.getName());
        Assert.assertEquals("iso6523-actorid-upis", result.getType());
        Assert.assertEquals(PROVIDED_VALUE, result.getValue());
    }

    @Test
    public void testTranslateParticipantIdentifierToRecipient_nullScheme() {
        As4MessageProperty result = TransmissionRequestUtil.toAs4MessageProperty("hello", ParticipantIdentifier.of(PROVIDED_VALUE, null));

        Assert.assertEquals("hello", result.getName());
        Assert.assertNull(result.getType());
        Assert.assertEquals(PROVIDED_VALUE, result.getValue());
    }

    @Test
    public void testTranslateParticipantIdentifierToRecipient_providedScheme() {
        As4MessageProperty result = TransmissionRequestUtil.toAs4MessageProperty("hello", ParticipantIdentifier.of(PROVIDED_VALUE, Scheme.of(PROVIDED_SCHEME)));

        Assert.assertEquals("hello", result.getName());
        Assert.assertEquals(PROVIDED_SCHEME, result.getType());
        Assert.assertEquals(PROVIDED_VALUE, result.getValue());
    }
}
