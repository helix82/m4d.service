package su.opencode.digsigcik.synapi.util;

import lombok.Getter;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class CertUtils {
    public static Map<String, Object> certInfo(byte[] certContent) throws CertificateException {
        HashMap<String, Object> ret = new HashMap<>();
        if(certContent != null) {
            CertificateFactory fac = CertificateFactory.getInstance("X509");
            X509Certificate cert = (X509Certificate) fac.generateCertificate(new ByteArrayInputStream(certContent));

            ret.put("issuer", String.valueOf(cert.getIssuerX500Principal().getName()));

            X500Name x500name = new JcaX509CertificateHolder(cert).getIssuer();
            RDN cn = x500name.getRDNs(BCStyle.CN)[0];

            ret.put("cn", IETFUtils.valueToString(cn.getFirst().getValue()));

            ret.put("type", String.valueOf(cert.getType()));
            ret.put("algName", String.valueOf(cert.getSigAlgName()));
            ret.put("notBefore", cert.getNotBefore());
            ret.put("notAfter", cert.getNotAfter());
            ret.put("serialNumber", String.valueOf(cert.getSerialNumber()));
            ret.put("status", "valid");
            if(cert.getNotAfter().before(new Date())) {
                ret.put("status", "expired");
            }
            if(cert.getNotBefore().after(new Date())) {
                ret.put("status", "not_arrived");
            }
        }
        return ret;
    }
}
