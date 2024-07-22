package su.opencode.digsigcik.synapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.Store;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.dto.ReportContainerDTO;
import su.opencode.digsigcik.synapi.dto.ReportDTO;
import su.opencode.digsigcik.synapi.enums.HashAlgorithm;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Qualifier("bouncy-castle")
public class SignatureVerificationServiceBCImpl implements SignatureVerificationService {

    @Override
    public ReportDTO verifyDetached(byte[] srcFile, byte[] detachedSign, String sourceFileFullName) {
        try {
            CMSSignedData signedData = new CMSSignedData(new CMSProcessableByteArray(srcFile), detachedSign);
            return processSignatureVerification(signedData, sourceFileFullName);
        } catch (CertificateException |CMSException | OperatorCreationException | IOException e) {
            return buildErrorReport(sourceFileFullName, "Подпись недействительна",
                    "Не удалось получить информацию о подписи");
        }
    }

    @Override
    public ReportDTO verifyAttached(byte[] attachedSign, String sourceFileFullName) {
        try {
            CMSSignedData signedData = new CMSSignedData(attachedSign);
            return processSignatureVerification(signedData, sourceFileFullName);
        } catch (CertificateException | CMSException | OperatorCreationException | IOException e) {
            return buildErrorReport(sourceFileFullName, "Подпись недействительна",
                    "Не удалось получить информацию о подписи");
        }
    }

    @Override
    public ReportContainerDTO verifyDetached(MultipartFile sourceFile, MultipartFile detachedSign) {
        try {
            ReportDTO reportDTO = verifyDetached(sourceFile.getBytes(), detachedSign.getBytes(), sourceFile.getOriginalFilename());
            return buildReportContainerDTO(reportDTO);
        } catch (Exception e) {
            // Handle exception
            return new ReportContainerDTO(new byte[0], new byte[0]); // Placeholder for error handling
        }
    }

    @Override
    public ReportContainerDTO verifyAttached(MultipartFile attachedSign) {
        try {
            ReportDTO reportDTO = verifyAttached(attachedSign.getBytes(), attachedSign.getOriginalFilename());
            return buildReportContainerDTO(reportDTO);
        } catch (Exception e) {
            // Handle exception
            return new ReportContainerDTO(new byte[0], new byte[0]); // Placeholder for error handling
        }
    }

    private ReportDTO processSignatureVerification(CMSSignedData signedData, String sourceFileFullName)
            throws CMSException, OperatorCreationException, IOException, CertificateException {
        SignerInformation signer = signedData.getSignerInfos().getSigners().iterator().next();
        Store<X509CertificateHolder> certStore = signedData.getCertificates();
        Collection<X509CertificateHolder> certCollection = certStore.getMatches(signer.getSID());
        X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(certCollection.iterator().next());

        ReportDTO.Result.VerificationResult.SignatureInfo signatureInfo = new ReportDTO.Result.VerificationResult.SignatureInfo();
        signatureInfo.setCAdESType(detectCAdESType(signedData));
        signatureInfo.setLocalSigningTime(ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        signatureInfo.setLocalSigningTimeUTC(ZonedDateTime.now().toEpochSecond());

        boolean isSignatureValid = signer.verify(new JcaSimpleSignerInfoVerifierBuilder().build(certificate));

        ReportDTO.Result.VerificationResult verificationResult = ReportDTO.Result.VerificationResult.builder()
                .result(isSignatureValid)
                .signerCertificateInfo(getSignerCertificateInfo(certCollection.iterator().next(), certificate))
                .signerCertificate(Base64.getEncoder().encodeToString(certificate.getEncoded()))
                .signatureInfo(signatureInfo)
                .build();

        ReportDTO.Result result = ReportDTO.Result.builder()
                .originalDocumentName(sourceFileFullName)
                .verificationTime(LocalDateTime.now().toString())
                .verificationResults(Collections.singletonList(verificationResult))
                .signatureType(detectSignatureType(signedData))
                .signatureDocumentHash(generateAndEncodeHash(signedData.getSignedContent().getContentType()
                        .getEncoded())) //TODO: not the same as in Crypto Pro
                .build();

        return ReportDTO.builder()
                .results(Collections.singletonList(result))
                .build();
    }

    private String detectSignatureType(CMSSignedData signedData) {
        ASN1ObjectIdentifier contentType = signedData.getSignedContent().getContentType();
        if (contentType.equals(CMSObjectIdentifiers.data)) {
            return "CMS";
        } else if (contentType.equals(CMSObjectIdentifiers.signedData)) {
            return "CAdES";
        } else {
            return "Unknown";
        }
    }

    private String detectCAdESType(CMSSignedData signedData) {
        if (signedData.getSignedContent().getContentType().equals(CMSObjectIdentifiers.signedData)) {
            ASN1Sequence cmsContentInfo = ASN1Sequence.getInstance(signedData.getSignedContent().getContent());
            ASN1Encodable signedContentInfo = cmsContentInfo.getObjectAt(0);
            if (signedContentInfo instanceof ASN1Sequence) {
                ASN1Sequence signedContentInfoSequence = (ASN1Sequence) signedContentInfo;
                ASN1ObjectIdentifier contentType = (ASN1ObjectIdentifier) signedContentInfoSequence.getObjectAt(0);
                if (contentType.equals(CMSObjectIdentifiers.data)) {
                    return "CAdES-BES";
                } else if (contentType.equals(CMSObjectIdentifiers.signedData)) {
                    return "CAdES-T";
                } else {
                    return "CAdES-Unknown";
                }
            } else if (signedContentInfo instanceof ASN1TaggedObject) {
                ASN1TaggedObject taggedObject = (ASN1TaggedObject) signedContentInfo;
                if (taggedObject.getTagNo() == 0) {
                    return "CAdES-BES";
                } else {
                    return "CAdES-Unknown";
                }
            } else {
                return "CAdES-Unknown";
            }
        } else {
            return "Unknown";
        }
    }

    private ReportDTO.Result.VerificationResult.SignerCertificateInfo getSignerCertificateInfo(X509CertificateHolder certHolder, X509Certificate certificate) {
        ReportDTO.Result.VerificationResult.SignerCertificateInfo signerCertificateInfo = new ReportDTO.Result.VerificationResult.SignerCertificateInfo();
        signerCertificateInfo.setSubjectName(convertEncodedEmail(certificate.getSubjectX500Principal().getName()));
        signerCertificateInfo.setIssuerName(convertEncodedEmail(certificate.getIssuerX500Principal().getName()));
        signerCertificateInfo.setNotBefore(certificate.getNotBefore().toString());
        signerCertificateInfo.setNotBeforeUTC(certificate.getNotBefore().getTime());
        signerCertificateInfo.setNotAfter(certificate.getNotAfter().toString());
        signerCertificateInfo.setNotAfterUTC(certificate.getNotAfter().getTime());
        signerCertificateInfo.setSerialNumber(certificate.getSerialNumber().toString());
        signerCertificateInfo.setThumbprint(getCertificateThumbprint(certHolder).toUpperCase());
        signerCertificateInfo.setPkNotBeforeUTC(certificate.getNotBefore().getTime());
        signerCertificateInfo.setPkNotAfterUTC(certificate.getNotAfter().getTime());
        signerCertificateInfo.setKeyIdentifier(getKeyIdentifier(certificate));
        return signerCertificateInfo;
    }

    private String convertEncodedEmail(String stringWithEncodedEmail) {
        String emailAttribute = "1.2.840.113549.1.9.1=#";
        int emailStart = stringWithEncodedEmail.indexOf(emailAttribute);
        if (emailStart != -1) {
            int emailEnd = stringWithEncodedEmail.indexOf(",", emailStart);
            if (emailEnd == -1) {
                emailEnd = stringWithEncodedEmail.length();
            }
            String oidString = stringWithEncodedEmail.substring(emailStart, emailEnd);
            String decodedEmail = oidString.substring(emailAttribute.length());
            // Remove unwanted characters (\u0016, \u0014)
            String cleanedHexEmail = decodedEmail.replaceAll("(1616)|(1617)|(1614)", "");
            byte[] bytes = hexStringToByteArray(cleanedHexEmail);
            return stringWithEncodedEmail.replace(oidString, "E=" + new String(bytes, StandardCharsets.UTF_8));
        } else {
            return stringWithEncodedEmail;
        }
    }

    private String getCertificateThumbprint(X509CertificateHolder certHolder) {
        try {
            byte[] encodedCertificate = certHolder.getEncoded();
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(encodedCertificate);
            return bytesToHex(digest);
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException("Error getting certificate thumbprint", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String getKeyIdentifier(X509Certificate certificate) {
        // Implement logic to get the key identifier
        return "NOT IMPLEMENTED";
    }

    private ReportDTO buildErrorReport(String sourceFileFullName, String error, String errorDescription) {
        ReportDTO.Result.VerificationResult verificationResult = ReportDTO.Result.VerificationResult.builder()
                .result(false)
                .build();
        ReportDTO.Result result = ReportDTO.Result.builder()
                .originalDocumentName(sourceFileFullName)
                .verificationTime(LocalDateTime.now().toString())
                .verificationResults(Collections.singletonList(verificationResult))
                .build();
        return ReportDTO.builder()
                .results(Collections.singletonList(result))
                .error(error)
                .errorDescription(errorDescription)
                .build();
    }

    private ReportContainerDTO buildReportContainerDTO(ReportDTO reportDTO) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(reportDTO);
            return ReportContainerDTO.builder()
                    .signCheck(jsonString.getBytes())
                    .signReport(new byte[0])
                    .build();
        } catch (IOException e) {
            // Handle exception
            return new ReportContainerDTO(new byte[0], new byte[0]); // Placeholder for error handling
        }
    }

    private String generateAndEncodeHash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HashAlgorithm.SHA256.getAlgorithmName());
            byte[] hash = digest.digest(data);
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hash algorithm not found", e);
        }
    }

    // Convert hex string to byte array
    private byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

}
