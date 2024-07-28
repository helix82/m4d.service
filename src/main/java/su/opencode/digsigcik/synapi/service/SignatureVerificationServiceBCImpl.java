package su.opencode.digsigcik.synapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.DocumentException;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.Store;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.dto.ReportContainerDTO;
import su.opencode.digsigcik.synapi.dto.ReportDTO;
import su.opencode.digsigcik.synapi.enums.HashAlgorithm;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import static su.opencode.digsigcik.synapi.util.ReportUtils.createPdf;

@Service
@Qualifier("bouncy-castle")
public class SignatureVerificationServiceBCImpl implements SignatureVerificationService {


    @Override
    public ReportContainerDTO verifyDetachedGetPdfReport(byte @NotNull [] sourceFile,
                                                         byte @NotNull [] detachedSignFile, String sourceFileName,
                                                         String detachedSignFileName) throws IOException,
            URISyntaxException {
        try {
            ReportDTO reportDTO = verifyDetached(sourceFile, detachedSignFile, sourceFileName, detachedSignFileName);
            return buildReportContainerDTO(reportDTO);
        } catch (Exception e) {
            // Handle exception
            return new ReportContainerDTO(new byte[0], new byte[0], false); // Placeholder for error handling
        }
    }

    @Override
    public ReportContainerDTO verifyDetachedGetPdfReport(@NotNull MultipartFile sourceFile,
                                                         @NotNull MultipartFile detachedSignFile) throws IOException,
            URISyntaxException {
        return verifyDetachedGetPdfReport(sourceFile.getBytes(), detachedSignFile.getBytes(),
                sourceFile.getOriginalFilename(), detachedSignFile.getOriginalFilename());
    }

    @Override
    public ReportContainerDTO verifyAttachedGetPdfReport(byte @NotNull [] attachedSign, String attachedSignFileName) throws IOException, URISyntaxException {
        try {
            ReportDTO reportDTO = verifyAttached(attachedSign, attachedSignFileName);
            return buildReportContainerDTO(reportDTO);
        } catch (Exception e) {
            // Handle exception
            return new ReportContainerDTO(new byte[0], new byte[0], false); // Placeholder for error handling
        }
    }

    @Override
    public ReportContainerDTO verifyAttachedGetPdfReport(@NotNull MultipartFile attachedSign) throws Exception {
        return verifyAttachedGetPdfReport(attachedSign.getBytes(),attachedSign.getOriginalFilename());
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
    public ReportDTO verifyDetached(byte[] sourceFile, byte[] detachedSign, String sourceFileFullName,
                                    String detachedSignFileName) throws Exception {
        try {
            CMSSignedData signedData = new CMSSignedData(new CMSProcessableByteArray(sourceFile), detachedSign);
            return processSignatureVerification(signedData, sourceFileFullName);
        } catch (CertificateException | CMSException | OperatorCreationException | IOException e) {
            return buildErrorReport(sourceFileFullName, "Подпись недействительна",
                    "Не удалось получить информацию о подписи");
        }
    }


    private ReportDTO processSignatureVerification(CMSSignedData signedData, String sourceFileFullName)
            throws CMSException, OperatorCreationException, IOException, CertificateException {
        SignerInformation signer = signedData.getSignerInfos()
                .getSigners()
                .iterator()
                .next();
        Store<X509CertificateHolder> certStore = signedData.getCertificates();
        Collection<X509CertificateHolder> certCollection = certStore.getMatches(signer.getSID());
        X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(certCollection.iterator()
                .next());

        ReportDTO.Result.VerificationResult.SignatureInfo signatureInfo =
                new ReportDTO.Result.VerificationResult.SignatureInfo();
        signatureInfo.setCAdESType(detectCAdESType(signedData));
        signatureInfo.setLocalSigningTime(ZonedDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        signatureInfo.setLocalSigningTimeUTC(ZonedDateTime.now()
                .toEpochSecond());

        boolean isSignatureValid = signer.verify(new JcaSimpleSignerInfoVerifierBuilder().build(certificate));

        ReportDTO.Result.VerificationResult verificationResult = ReportDTO.Result.VerificationResult.builder()
                .result(isSignatureValid)
                .signerCertificateInfo(getSignerCertificateInfo(certCollection.iterator()
                        .next(), certificate))
                .signerCertificate(Base64.getEncoder()
                        .encodeToString(certificate.getEncoded()))
                .signatureInfo(signatureInfo)
                .build();

        ReportDTO.Result result = ReportDTO.Result.builder()
                .originalDocumentName(sourceFileFullName)
                .verificationTime(LocalDateTime.now()
                        .toString())
                .verificationResults(Collections.singletonList(verificationResult))
                .signatureType(detectSignatureType(signedData))
                .signatureDocumentHash(generateAndEncodeHash(signedData.getSignedContent()
                        .getContentType()
                        .getEncoded())) //TODO: not the same as in Crypto Pro
                .build();

        return ReportDTO.builder()
                .results(Collections.singletonList(result))
                .build();
    }

    public String detectSignatureType(CMSSignedData signedData) {
        ASN1ObjectIdentifier contentType = signedData.getSignedContent()
                .getContentType();
        if (contentType.equals(CMSObjectIdentifiers.data)) {
            return "CMS"; // Для контент-типов типа данных
        } else if (contentType.equals(CMSObjectIdentifiers.signedData)) {
            return "CAdES"; // Для CAdES подписанных данных
        } else {
            return "Unknown"; // Для неизвестных типов
        }
    }

    public static String detectCAdESType(CMSSignedData signedData) {
        if (CMSObjectIdentifiers.signedData.equals(signedData.getSignedContent()
                .getContentType())) {
            ASN1Sequence cmsContentInfo = ASN1Sequence.getInstance(signedData.toASN1Structure()
                    .getContent());
            ASN1Encodable signedContentInfo = (ASN1Encodable) cmsContentInfo.getObjectAt(0);
            if (signedContentInfo instanceof ASN1Sequence) {
                ASN1Sequence signedContentInfoSequence = (ASN1Sequence) signedContentInfo;
                ASN1ObjectIdentifier contentType = (ASN1ObjectIdentifier) signedContentInfoSequence.getObjectAt(0);
                if (CMSObjectIdentifiers.data.equals(contentType)) {
                    return "CAdES-BES";
                } else if (CMSObjectIdentifiers.signedData.equals(contentType)) {
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
        ReportDTO.Result.VerificationResult.SignerCertificateInfo signerCertificateInfo =
                new ReportDTO.Result.VerificationResult.SignerCertificateInfo();
        signerCertificateInfo.setSubjectName(convertEncodedEmail(certificate.getSubjectX500Principal()
                .getName()));
        signerCertificateInfo.setIssuerName(convertEncodedEmail(certificate.getIssuerX500Principal()
                .getName()));
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
        signerCertificateInfo.setThumbprint(getCertificateThumbprint(certHolder).toUpperCase());
        signerCertificateInfo.setPkNotBeforeUTC(certificate.getNotBefore()
                .getTime());
        signerCertificateInfo.setPkNotAfterUTC(certificate.getNotAfter()
                .getTime());
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
                .verificationTime(LocalDateTime.now()
                        .toString())
                .verificationResults(Collections.singletonList(verificationResult))
                .build();
        return ReportDTO.builder()
                .results(Collections.singletonList(result))
                .error(error)
                .errorDescription(errorDescription)
                .build();
    }

    private ReportContainerDTO buildReportContainerDTO(ReportDTO reportDTO) throws DocumentException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(reportDTO);
            AtomicBoolean valid = new AtomicBoolean(false);
            reportDTO.getResults()
                    .stream()
                    .flatMap(result -> result.getVerificationResults()
                            .stream())
                    .findFirst()
                    .ifPresent(verificationResult -> {
                        valid.set(verificationResult.isResult());
                    });
            return ReportContainerDTO.builder()
                    .signCheck(jsonString.getBytes())
                    .signReport(createPdf(reportDTO))
                    .valid(valid.get())
                    .build();
        } catch (IOException e) {
            // Handle exception
            return new ReportContainerDTO(new byte[0], new byte[0], false); // Placeholder for error handling
        }
    }

    private String generateAndEncodeHash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HashAlgorithm.SHA256.getAlgorithmName());
            byte[] hash = digest.digest(data);
            return Base64.getEncoder()
                    .encodeToString(hash);
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
