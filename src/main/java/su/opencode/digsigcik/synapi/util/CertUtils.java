package su.opencode.digsigcik.synapi.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.util.Store;
import su.opencode.digsigcik.synapi.model.RevokedCertificate;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.*;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Slf4j
public class CertUtils {
    public static Map<String, Object> certInfo(String certContent) throws CertificateException {
        HashMap<String, Object> ret = new HashMap<>();
        if(certContent != null) {
            CertificateFactory fac = CertificateFactory.getInstance("X509");
            X509Certificate cert = (X509Certificate) fac.generateCertificate(new ByteArrayInputStream(certContent.getBytes()));

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

    public static List<X509Certificate> getX509CertificatesFromDigitalSign(byte[] signedData, byte[] sign) {

        try {
            return getX509CertificatesFromDigitalSign(new CMSSignedData(new CMSProcessableByteArray(signedData), sign));
        } catch (CMSException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    public static List<X509Certificate> getX509CertificatesFromDigitalSign(byte[] signedDataAndSign) {

        try {
            return getX509CertificatesFromDigitalSign( new CMSSignedData(signedDataAndSign) );
        } catch (CMSException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    public static List<X509Certificate> getX509CertificatesFromDigitalSign(CMSSignedData signedData) {

        SignerInformation signer = signedData.getSignerInfos().getSigners().iterator().next();
        Store<X509CertificateHolder> certStore = signedData.getCertificates();
        Collection<X509CertificateHolder> certCollection = certStore.getMatches(signer.getSID());

        JcaX509CertificateConverter converter = new JcaX509CertificateConverter();

        return certCollection
                .stream()
                .map(x509CertificateHolder -> {
                    try {
                        return converter.getCertificate(x509CertificateHolder);
                    } catch (CertificateException ex) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static Collection<X509CRL> extractNativeCRLs(Collection<RevokedCertificate> crlsInsideRevokedCertificates) {

        return crlsInsideRevokedCertificates
                .stream()
                .map(revokedCertificateCRL -> {

                    try (InputStream inputStream = new BufferedInputStream( new ByteArrayInputStream(
                            Base64.getDecoder().decode(revokedCertificateCRL.getCertificate())
                    ))) {

                        // log.trace("CRL file size: " + crl.getRevokedCertificates().size());
                        return (X509CRL) CertificateFactory.getInstance("x509").generateCRL(inputStream);

                    } catch (CertificateException | CRLException | IOException | IllegalArgumentException e) {
                        log.error("Something went wrong while deserializing X509 entities: " + e.getMessage());
                        return null;        // skip that broken crl
                    }

                })
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableList());
    }
}
