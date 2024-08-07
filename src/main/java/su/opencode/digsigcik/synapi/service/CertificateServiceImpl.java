package su.opencode.digsigcik.synapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.shredzone.acme4j.Login;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import su.opencode.digsigcik.synapi.model.AcmeProvider;
import su.opencode.digsigcik.synapi.repository.ProviderRepository;
import su.opencode.digsigcik.synapi.util.DateUtils;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateServiceImpl implements CertificateService {

    @Autowired
    ProviderRepository providerRepository;

    @Override
    public byte[] certificateOrderCSR(String domain, Map<String, Object> data) {

        String organization = "";
        String commonName = domain;
        String organizationalUnit = "";
        String locality = "";
        String state = "";
        String country = "";

        if(data.containsKey("state")) state = String.valueOf(data.get("state"));
        if(data.containsKey("country")) country = String.valueOf(data.get("country"));
        if(data.containsKey("locality")) locality = String.valueOf(data.get("locality"));
        if(data.containsKey("organization")) organization = String.valueOf(data.get("organization"));
        if(data.containsKey("commonName")) commonName = String.valueOf(data.get("commonName"));
        if(data.containsKey("ST")) state = String.valueOf(data.get("ST"));
        if(data.containsKey("C")) country = String.valueOf(data.get("C"));
        if(data.containsKey("L")) locality = String.valueOf(data.get("L"));
        if(data.containsKey("O")) organization = String.valueOf(data.get("O"));
        if(data.containsKey("CN")) commonName = String.valueOf(data.get("CN"));
        if(data.containsKey("OU")) organizationalUnit = String.valueOf(data.get("OU"));
        if(data.containsKey("region") || data.containsKey("city")) {
            country = String.valueOf("RU");
            if(data.containsKey("region")) state = String.valueOf(data.get("region"));
            if(data.containsKey("city")) locality = String.valueOf(data.get("city"));
        }
        KeyPair keyPair = null;
        try {
            keyPair = generateKeyPair();
            PKCS10CertificationRequest csr = generateCSR(keyPair, commonName, organization, organizationalUnit, locality, state, country);


            StringWriter writer = new StringWriter();
            JcaPEMWriter pemWriter = new JcaPEMWriter(writer);
            pemWriter.writeObject(csr);
            pemWriter.close();
            writer.close();

            return writer.toString().getBytes();

        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (CertificateException e1) {
            e1.printStackTrace();
        } catch (SignatureException e1) {
            e1.printStackTrace();
        } catch (OperatorCreationException e1) {
            e1.printStackTrace();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return null;
    }

    @Override
    public byte[] createRootCertificate(String notAfter, Map<String, Object> data) {

        String organization = "";
        String commonName = "";
        String organizationalUnit = "";
        String locality = "";
        String state = "";
        String country = "";

        if(data.containsKey("state")) state = String.valueOf(data.get("state"));
        if(data.containsKey("country")) country = String.valueOf(data.get("country"));
        if(data.containsKey("locality")) locality = String.valueOf(data.get("locality"));
        if(data.containsKey("organization")) organization = String.valueOf(data.get("organization"));
        if(data.containsKey("commonName")) commonName = String.valueOf(data.get("commonName"));
        if(data.containsKey("ST")) state = String.valueOf(data.get("ST"));
        if(data.containsKey("C")) country = String.valueOf(data.get("C"));
        if(data.containsKey("L")) locality = String.valueOf(data.get("L"));
        if(data.containsKey("O")) organization = String.valueOf(data.get("O"));
        if(data.containsKey("CN")) commonName = String.valueOf(data.get("CN"));
        if(data.containsKey("OU")) organizationalUnit = String.valueOf(data.get("OU"));
        if(data.containsKey("region") || data.containsKey("city")) {
            country = String.valueOf("RU");
            if(data.containsKey("region")) state = String.valueOf(data.get("region"));
            if(data.containsKey("city")) locality = String.valueOf(data.get("city"));
        }

        KeyPair keyPair = null;
        try {
            keyPair = generateKeyPair();
            X509Certificate cert = generateSelfSignedRootCertificate(keyPair, notAfter, commonName, organization, organizationalUnit, locality, state, country);

            StringWriter writer = new StringWriter();
            JcaPEMWriter pemWriter = new JcaPEMWriter(writer);
            pemWriter.writeObject(cert);
            pemWriter.close();
            writer.close();

            return writer.toString().getBytes();

        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (CertificateException e1) {
            e1.printStackTrace();
        } catch (SignatureException e1) {
            e1.printStackTrace();
        } catch (OperatorCreationException e1) {
            e1.printStackTrace();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return null;
    }

    private static PKCS10CertificationRequest generateCSR(KeyPair keyPair, String commonName, String organization,
                                                          String organizationalUnit, String locality, String state, String country)
            throws OperatorCreationException {

        X500Name x500Name = new X500NameBuilder(BCStyle.INSTANCE)
                .addRDN(BCStyle.CN, commonName)
                .addRDN(BCStyle.O, organization)
                .addRDN(BCStyle.OU, organizationalUnit)
                .addRDN(BCStyle.L, locality)
                .addRDN(BCStyle.ST, state)
                .addRDN(BCStyle.C, country)
                .build();

        PKCS10CertificationRequestBuilder csrBuilder = new JcaPKCS10CertificationRequestBuilder(x500Name, keyPair.getPublic());
        JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder("SHA256withRSA");
        csBuilder.setProvider("BC");
        ContentSigner signer = csBuilder.build(keyPair.getPrivate());
        return csrBuilder.build(signer);
    }

    private static X509Certificate generateSelfSignedRootCertificate(KeyPair keyPair, String notAfter, String commonName, String organization,
                                                                 String organizationalUnit, String locality, String state, String country) throws IOException, OperatorCreationException, CertificateException {

        X500Name issuerName = new X500NameBuilder(BCStyle.INSTANCE)
                .addRDN(BCStyle.CN, commonName)
                .addRDN(BCStyle.O, organization)
                .addRDN(BCStyle.OU, organizationalUnit)
                .addRDN(BCStyle.L, locality)
                .addRDN(BCStyle.ST, state)
                .addRDN(BCStyle.C, country)
                .build();

        BigInteger serialNumber = new BigInteger(64, new SecureRandom());
        Date startDate = new Date();
        Date endDate = new Date(startDate.getTime() + (365 * 24 * 60 * 60 * 1000L)); // 1 year validity

        try {
            endDate = DateUtils.parse(notAfter);
        } catch (ParseException ignored) {}

        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(issuerName, serialNumber, startDate, endDate, issuerName, keyPair.getPublic());

        certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
        certBuilder.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign));

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").setProvider("BC").build(keyPair.getPrivate());
        X509CertificateHolder certHolder = certBuilder.build(signer);

        return new JcaX509CertificateConverter().setProvider("BC").getCertificate(certHolder);
    }

    private static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }
}

