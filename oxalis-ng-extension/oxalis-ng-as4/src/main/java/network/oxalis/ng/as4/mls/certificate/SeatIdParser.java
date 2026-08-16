package network.oxalis.ng.as4.mls.certificate;

import network.oxalis.ng.as4.mls.exception.InvalidCertificateException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SeatIdParser {

    private static final Pattern SEAT_ID_PATTERN = Pattern.compile("^P[A-Z]{2}([0-9]{6})$");

    public String parseMainId(String seatId) {
        if (seatId == null) {
            throw new InvalidCertificateException("Certificate Subject CN is absent");
        }

        Matcher matcher = SEAT_ID_PATTERN.matcher(seatId.trim());
        if (!matcher.matches()) {
            throw new InvalidCertificateException("Certificate Subject CN '" + seatId + "' does not match the Peppol Seat ID grammar");
        }

        return matcher.group(1);
    }
}
