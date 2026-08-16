package network.oxalis.ng.as4.mls.certificate;

import network.oxalis.ng.as4.mls.exception.InvalidCertificateException;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class SeatIdParserTest {

    private final SeatIdParser parser = new SeatIdParser();

    @Test
    public void parsesMainIdFromSeatId() {
        assertEquals(parser.parseMainId("POP000723"), "000723");
    }

    @Test
    public void trimsWhitespace() {
        assertEquals(parser.parseMainId("  POP000723  "), "000723");
    }

    @Test(expectedExceptions = InvalidCertificateException.class)
    public void nullSeatIdThrows() {
        parser.parseMainId(null);
    }

    @Test(expectedExceptions = InvalidCertificateException.class)
    public void wrongPrefixThrows() {
        parser.parseMainId("XOP000723");
    }

    @Test(expectedExceptions = InvalidCertificateException.class)
    public void tooFewDigitsThrows() {
        parser.parseMainId("POP12345");
    }

    @Test(expectedExceptions = InvalidCertificateException.class)
    public void arbitraryCnThrows() {
        parser.parseMainId("Some Access Point");
    }
}
