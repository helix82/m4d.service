package su.opencode.digsigcik.synapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.dto.ReportContainerDTO;
import su.opencode.digsigcik.synapi.dto.ReportDTO;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static su.opencode.digsigcik.synapi.util.HashUtils.calculateSignatureDocumentHash;

@Service
@Qualifier("bouncy-castle")
public class SignatureVerificationServiceBCImpl implements SignatureVerificationService {

    private final ContainerService containerService;


    public SignatureVerificationServiceBCImpl(ContainerService containerService) {
        this.containerService = containerService;
    }

    @Override
    public ReportDTO verifyDetached(byte[] srcFile, byte[] detachedSign, String sourceFileFullName) throws Exception {
        CMSProcessable signedContent = new CMSProcessableByteArray(srcFile);
        CMSSignedData signedData;
        try {
            signedData = new CMSSignedData(signedContent, detachedSign);
        } catch (CMSException e) {
            ReportDTO.Result.VerificationResult verificationResult = ReportDTO.Result.VerificationResult.builder()
                    .result(false)
                    .build();
            ReportDTO.Result result = ReportDTO.Result.builder()
                    .originalDocumentName(sourceFileFullName)
                    .signedDocumentName(sourceFileFullName)
                    .verificationTime(LocalDateTime.now()
                            .toString())
                    .verificationResults(Collections.singletonList(verificationResult))
                    .build();
            return ReportDTO.builder()
                    .results(Collections.singletonList(result))
                    .error("Подпись недействительна")
                    .errorDescription("Не удалось получить информацию о подписи")
                    .build();
        }

        SignerInformation signer;
        try {
            Store<X509CertificateHolder> certStoreInSing = signedData.getCertificates();
            signer = signedData.getSignerInfos()
                    .getSigners()
                    .iterator()
                    .next();

            Collection certCollection = certStoreInSing.getMatches(signer.getSID());
            Iterator certIt = certCollection.iterator();
            X509CertificateHolder certHolder = (X509CertificateHolder) certIt.next();
            X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(certHolder);
            ReportDTO.Result.VerificationResult verificationResult = ReportDTO.Result.VerificationResult.builder()
                    .result(signer.verify(new JcaSimpleSignerInfoVerifierBuilder().build(certificate)))
                    .signerCertificateInfo(getSignerCertificateInfo(certHolder,certificate))
                    .signerCertificate(Base64.toBase64String(certificate.getEncoded()))
                    .signatureInfo(getSignatureInfo(signedData,certHolder))
                    .build();
            ReportDTO.Result result = ReportDTO.Result.builder()
                    .originalDocumentName(sourceFileFullName)
                    //      .SignedDocumentName(signFile.getOriginalFilename())
                    .verificationTime(LocalDateTime.now()
                            .toString())
                    .verificationResults(Collections.singletonList(verificationResult))
                    .signatureType(detectSignatureType(signedData))
                    .build();
            return ReportDTO.builder()
                    .results(Collections.singletonList(result))
                    .build();
        } catch (Exception ex) {
            ReportDTO.Result.VerificationResult verificationResult = ReportDTO.Result.VerificationResult.builder()
                    .result(false)
                    .build();
            ReportDTO.Result result = ReportDTO.Result.builder()
                    .originalDocumentName(sourceFileFullName)
                    //    .SignedDocumentName(signFile.getOriginalFilename())
                    .verificationTime(LocalDateTime.now()
                            .toString())
                    .verificationResults(Collections.singletonList(verificationResult))
                    .signatureType(detectSignatureType(signedData))
                    .build();
            return ReportDTO.builder()
                    .results(Collections.singletonList(result))
                    .error("Подпись недействительна")
                    .errorDescription("Не удалось получить информацию о подписи")
                    .build();

        }
    }

    public static String detectSignatureType(CMSSignedData signedData) {
        if (signedData.getSignedContent().getContentType().equals(CMSObjectIdentifiers.data)) {
            return  "CMS";
        } else if (signedData.getSignedContent().getContentType().equals(CMSObjectIdentifiers.signedData)) {
            return "CAdES";
        } else {
            return "Unknown";
        }
    }

    public static String detectCAdESType(CMSSignedData signedData) {
        // Determine the signature type
        String signatureType;
        if (signedData.getSignedContent().getContentType().equals(CMSObjectIdentifiers.signedData)) {
            // Detect the CAdES type
            ASN1Sequence cmsContentInfo = (ASN1Sequence) signedData.getSignedContent().getContent();
            ASN1Encodable signedContentInfo = cmsContentInfo.getObjectAt(0);
            if (signedContentInfo instanceof ASN1Sequence) {
                ASN1Sequence signedContentInfoSequence = (ASN1Sequence) signedContentInfo;
                ASN1ObjectIdentifier contentType = (ASN1ObjectIdentifier) signedContentInfoSequence.getObjectAt(0);
                if (contentType.equals(CMSObjectIdentifiers.data)) {
                    signatureType = "CAdES-BES";
                } else if (contentType.equals(CMSObjectIdentifiers.signedData)) {
                    signatureType = "CAdES-T";
                } else {
                    signatureType = "CAdES-Unknown";
                }
            } else if (signedContentInfo instanceof ASN1TaggedObject) {
                ASN1TaggedObject taggedObject = (ASN1TaggedObject) signedContentInfo;
                if (taggedObject.getTagNo() == 0) {
                    signatureType = "CAdES-BES";
                } else {
                    signatureType = "CAdES-Unknown";
                }
            } else {
                signatureType = "CAdES-Unknown";
            }
        } else {
            signatureType = "Unknown";
        }


        return signatureType;
    }

    @Override
    public ReportDTO verifyAttached(byte[] attachedSign, String sourceFileFullName) throws Exception {
        CMSSignedData signedData;
        try {
            signedData = new CMSSignedData(attachedSign);
        } catch (CMSException e) {
            ReportDTO.Result.VerificationResult verificationResult = ReportDTO.Result.VerificationResult.builder()
                    .result(false)
                    .build();
            ReportDTO.Result result = ReportDTO.Result.builder()
                    //     .OriginalDocumentName(sourceFile.getOriginalFilename())
                    .signedDocumentName(sourceFileFullName)
                    .verificationTime(LocalDateTime.now()
                            .toString())
                    .verificationResults(Collections.singletonList(verificationResult))
                    .build();
            return ReportDTO.builder()
                    .results(Collections.singletonList(result))
                    .error("Подпись недействительна")
                    .errorDescription("Не удалось получить информацию о подписи")
                    .build();
        }

        SignerInformation signer;
        try {

            Store<X509CertificateHolder> certStoreInSing = signedData.getCertificates();
            signer = signedData.getSignerInfos()
                    .getSigners()
                    .iterator()
                    .next();

            Collection certCollection = certStoreInSing.getMatches(signer.getSID());
            Iterator certIt = certCollection.iterator();
            X509CertificateHolder certHolder = (X509CertificateHolder) certIt.next();
            X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(certHolder);
            ReportDTO.Result.VerificationResult verificationResult = ReportDTO.Result.VerificationResult.builder()
                    .result(signer.verify(new JcaSimpleSignerInfoVerifierBuilder().build(certificate)))
                    .signerCertificateInfo(getSignerCertificateInfo(certHolder,certificate))
                    .signerCertificate(Base64.toBase64String(certificate.getEncoded()))
                    .signatureInfo(getSignatureInfo(signedData,certHolder))
                    .build();
            ReportDTO.Result result = ReportDTO.Result.builder()
                    .originalDocumentName(sourceFileFullName)
                    //.SignedDocumentName(signFile.getOriginalFilename())
                    .verificationTime(LocalDateTime.now()
                            .toString())
                    .verificationResults(Collections.singletonList(verificationResult))
                    .signatureType(detectSignatureType(signedData))
                    .signatureDocumentHash(calculateSignatureDocumentHash(attachedSign))
                    .build();
            return ReportDTO.builder()
                    .results(Collections.singletonList(result))
                    .build();
        } catch (Exception ex) {
            ReportDTO.Result.VerificationResult verificationResult = ReportDTO.Result.VerificationResult.builder()
                    .result(false)
                    .build();
            ReportDTO.Result result = ReportDTO.Result.builder()
                    //     .OriginalDocumentName(sourceFile.getOriginalFilename())
                    .signedDocumentName(sourceFileFullName)
                    .verificationTime(LocalDateTime.now()
                            .toString())
                    .verificationResults(Collections.singletonList(verificationResult))
                    .build();
            return ReportDTO.builder()
                    .results(Collections.singletonList(result))
                    .error("Подпись недействительна")
                    .errorDescription("Не удалось получить информацию о подписи")
                    .build();

        }
    }

    @Override
    public ReportContainerDTO verifyDetached(@NotNull MultipartFile sourceFile, @NotNull MultipartFile detachedSign) throws Exception {

        ReportDTO reportDTO = verifyDetached(sourceFile.getBytes(),detachedSign.getBytes(), sourceFile.getOriginalFilename());
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(reportDTO);
        return ReportContainerDTO.builder()
                .signCheck(jsonString.getBytes())
                .signReport(new byte[0])
                .build();
    }

    @Override
    public ReportContainerDTO verifyAttached(@NotNull MultipartFile attachedSign) throws Exception {
        ReportDTO reportDTO = verifyAttached(attachedSign.getBytes(), attachedSign.getOriginalFilename());
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(reportDTO);
        return ReportContainerDTO.builder()
                .signCheck(jsonString.getBytes())
                .signReport(new byte[0])
                .build();
    }

    private static ReportDTO.Result.VerificationResult.SignatureInfo getSignatureInfo(CMSSignedData signedData, X509CertificateHolder certHolder) {
        ReportDTO.Result.VerificationResult.SignatureInfo signatureInfo =
                new ReportDTO.Result.VerificationResult.SignatureInfo();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        signatureInfo.setCAdESType(detectCAdESType(signedData));

        // Set the local signing time (assuming it's not available in the certificate)
        ZonedDateTime localSigningTime = ZonedDateTime.now();
        signatureInfo.setLocalSigningTime(localSigningTime.format(formatter));
        signatureInfo.setLocalSigningTimeUTC(localSigningTime.toEpochSecond());

        return signatureInfo;
    }


    public ReportDTO.Result.VerificationResult.SignerCertificateInfo getSignerCertificateInfo(X509CertificateHolder certHolder,X509Certificate certificate ) {
        ReportDTO.Result.VerificationResult.SignerCertificateInfo signerCertificateInfo =
                new ReportDTO.Result.VerificationResult.SignerCertificateInfo();

        signerCertificateInfo.setSubjectName(certificate.getSubjectX500Principal()
                .getName());
        signerCertificateInfo.setIssuerName(certificate.getIssuerX500Principal()
                .getName());
        signerCertificateInfo.setNotBefore(certificate.getNotBefore()
                .toString());
        signerCertificateInfo.setNotBeforeUTC(certificate.getNotBefore()
                .getTime());
        signerCertificateInfo.setNotAfter(certificate.getNotAfter()
                .toString());
        signerCertificateInfo.setNotAfterUTC(certificate.getNotAfter()
                .getTime());
        signerCertificateInfo.setSerialNumber(certificate.getSerialNumber()
                .toString());
        signerCertificateInfo.setThumbprint(getCertificateThumbprint(certHolder));
        signerCertificateInfo.setPkNotBeforeUTC(certificate.getNotBefore()
                .getTime());
        signerCertificateInfo.setPkNotAfterUTC(certificate.getNotAfter()
                .getTime());
        signerCertificateInfo.setKeyIdentifier(getKeyIdentifier(certificate));

        return signerCertificateInfo;
    }

    private static String getCertificateThumbprint(X509CertificateHolder certHolder) {
        try {
            byte[] encodedCertificate = certHolder.getEncoded();
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(encodedCertificate);
            return bytesToHex(digest);
        } catch (NoSuchAlgorithmException | IOException e) {
            // Handle the exception
            throw new RuntimeException("Error getting certificate thumbprint", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static String getKeyIdentifier(X509Certificate certificate) {
        // Implement the logic to get the key identifier
        // This is a placeholder implementation, you need to provide the actual implementation
        return "NOT IMPLEMENTED";
    }
    private static String getSignatureAlgorithm(X509Certificate signerCertificate) {
        return signerCertificate.getSigAlgName();
    }
    private static String getSignatureValue(CMSSignedData cmsSignedData) {
        SignerInformation signerInformation = (SignerInformation) cmsSignedData.getSignerInfos().getSigners().iterator().next();
        return Hex.toHexString(signerInformation.getSignature());
    }
}
