package network.oxalis.as4.outbound;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.as4.inbound.OxalisAS4Version;
import network.oxalis.commons.util.OxalisVersion;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

@Slf4j
@Singleton
public class BrowserTypeProvider {

    private final X509Certificate certificate;

    @Inject
    public BrowserTypeProvider(X509Certificate certificate) {
        this.certificate = certificate;
    }

    public String getBrowserType() {
        return String.format("Oxalis %s / AS4 %s / %s",
                OxalisVersion.getVersion(),
                OxalisAS4Version.getVersion(),
                getCN());
    }

    private String getCN() {
        try {
            X500Name x500name = new JcaX509CertificateHolder(certificate).getSubject();
            RDN cn = x500name.getRDNs(BCStyle.CN)[0];
            return IETFUtils.valueToString(cn.getFirst().getValue());
        } catch (CertificateEncodingException e) {
            log.warn("Could not extract CN from certificate", e);
            return "Unknown";
        }
    }
}
