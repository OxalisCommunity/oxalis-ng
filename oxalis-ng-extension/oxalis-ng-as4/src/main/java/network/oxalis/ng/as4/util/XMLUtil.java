package network.oxalis.ng.as4.util;

import lombok.experimental.UtilityClass;
import network.oxalis.ng.as4.lang.OxalisAs4Exception;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;

@UtilityClass
public class XMLUtil {

    public static XMLGregorianCalendar dateToXMLGeorgianCalendar(Date date) throws OxalisAs4Exception {
        try {
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(date);

            return DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
        } catch (DatatypeConfigurationException e) {
            throw new OxalisAs4Exception("Unable to convert timestamp to XML", e);
        }
    }
}
