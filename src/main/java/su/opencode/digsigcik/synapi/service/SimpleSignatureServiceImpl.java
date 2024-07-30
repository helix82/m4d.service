package su.opencode.digsigcik.synapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.DocumentException;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.dto.ContainerInputInfoDto;
import su.opencode.digsigcik.synapi.dto.ReportDTO;
import su.opencode.digsigcik.synapi.enums.KeySize;
import su.opencode.digsigcik.synapi.enums.SimpleSignatureAlgorithm;
import su.opencode.digsigcik.synapi.enums.TypeUserCertificateEnum;
import su.opencode.digsigcik.synapi.model.EncryptionKeyEntity;
import su.opencode.digsigcik.synapi.model.PublicKeyUserCertificate;
import su.opencode.digsigcik.synapi.model.UserProjection;
import su.opencode.digsigcik.synapi.repository.EncryptionKeyRepository;
import su.opencode.digsigcik.synapi.util.Constants;
import su.opencode.digsigcik.synapi.util.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.*;

import static su.opencode.digsigcik.synapi.util.ReportUtils.createPdf;

@Service
@Slf4j
public class SimpleSignatureServiceImpl implements SimpleSignatureService {

    private final ContainerService containerService;
    private final EncryptionKeyRepository encryptionKeyRepository;
    private final SignatureVerificationService signatureVerificationService;
    private final PublicKeyUserCertificateService publicKeyUserCertificateService;
    private final ObjectMapper objectMapper;
    @Value("${features.show-ses-certificates}")
    private boolean showSesCertificates;
    @Value("${features.ses-certificates-age}")
    private int sesCertificatesAge;
    @Value("${features.ses-certificates-issuer}")
    private String sesCertificatesIssuer;

    public SimpleSignatureServiceImpl(ContainerService containerService,
                                      EncryptionKeyRepository encryptionKeyRepository,
                                      @Qualifier("bouncy-castle") SignatureVerificationService signatureVerificationService, PublicKeyUserCertificateService publicKeyUserCertificateService,
                                      ObjectMapper objectMapper) {
        this.containerService = containerService;
        this.encryptionKeyRepository = encryptionKeyRepository;
        this.signatureVerificationService = signatureVerificationService;
        this.publicKeyUserCertificateService = publicKeyUserCertificateService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Resource createSimpleSignatureResource(@NotNull MultipartFile srcFile,
                                                  @NotNull ContainerInputInfoDto containerInputInfo) throws IOException {


        EncryptionKeyEntity keyEntity = getOrCreateKeyEntity(containerInputInfo);
        SimpleSignatureAlgorithm simpleSignatureAlgorithm = keyEntity.getAlgorithm();
        X509Certificate certificate;
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(keyEntity.getCert()));
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }
        KeyPair keyPair = getKeyPairByEncryptionKeyEntity(keyEntity);
        byte[] sign;
        ReportDTO reportDTO;
        try {
            if (containerInputInfo.isSignatureDetached()) {
                sign = createSignature(srcFile.getBytes(), keyPair.getPrivate(), certificate,
                        simpleSignatureAlgorithm.getAlgorithmName(), false);
                reportDTO = signatureVerificationService.verifyDetached(srcFile.getBytes(), sign,
                        srcFile.getOriginalFilename(), srcFile.getOriginalFilename() + ".p7s");
            } else {
                sign = createSignature(srcFile.getBytes(), keyPair.getPrivate(), certificate,
                        simpleSignatureAlgorithm.getAlgorithmName(), true);
                reportDTO = signatureVerificationService.verifyAttached(sign, srcFile.getOriginalFilename());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String sourceDirectoryPathName = Constants.ROOT_FILE_STORAGE_PATH + Constants.SOURCE_DIRECTORY_PATH;
        String sigFilePathName = sourceDirectoryPathName + "temp.p7s";
        FileUtils.createDirectoryIfSheNotExist(sourceDirectoryPathName);
        FileUtils.createFile(sigFilePathName, sign);

        MultipartFile sigMultipartFile = FileUtils.mapFileToMultipart(sigFilePathName);
        deleteFileIfExists(sigFilePathName);

        String signCheckJsonString = objectMapper.writeValueAsString(reportDTO);
        byte[] signCheck = signCheckJsonString.getBytes(StandardCharsets.UTF_8);
        containerInputInfo.setSignCheck(signCheck);
        try {
            containerInputInfo.setSignReport(createPdf(reportDTO));
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
        return containerService.createContainer(srcFile, sigMultipartFile, containerInputInfo);
    }
    @Transactional
    public void deleteKeyEntity(@NotNull UserProjection userProjection) {
        encryptionKeyRepository.deleteEncryptionKeyEntityByUserProjection(userProjection);
    }

    public EncryptionKeyEntity getOrCreateKeyEntity(@NotNull ContainerInputInfoDto containerInputInfo)  {
        EncryptionKeyEntity keyEntity =
                encryptionKeyRepository.getEncryptionKeyEntitiesByUserProjection(containerInputInfo.getUserProjection());
        return keyEntity == null ? createKeyEntity(containerInputInfo) : keyEntity;
    }

    public EncryptionKeyEntity createKeyEntity(@NotNull ContainerInputInfoDto containerInputInfo)  {
        String fullUserName = String.join(" ",
                containerInputInfo.getLastname(),
                containerInputInfo.getFirstname(),
                containerInputInfo.getPatronymic());
        SimpleSignatureAlgorithm simpleSignatureAlgorithm;
        simpleSignatureAlgorithm =
                SimpleSignatureAlgorithm.fromAlgorithmName(containerInputInfo.getSignatureAlgorithm());
        KeyPair keyPair = generateKeyPair(simpleSignatureAlgorithm, containerInputInfo.getKeySize());
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();
        EncryptionKeyEntity keyEntity = new EncryptionKeyEntity();
        keyEntity.setAlgorithm(simpleSignatureAlgorithm);
        keyEntity.setPrivateKey(privateKey.getEncoded());
        keyEntity.setPublicKey(publicKey.getEncoded());
        keyEntity.setUserProjection(containerInputInfo.getUserProjection());
        X509Certificate certificate = createSelfSignedCertificate(keyPair, simpleSignatureAlgorithm, fullUserName);
        try {
            keyEntity.setCert(certificate.getEncoded());
        } catch (CertificateEncodingException e) {
            log.error("Ошибка при записи сертификата: ", e);
        }
        if (showSesCertificates) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            formatter.setTimeZone(TimeZone.getTimeZone("UTC")); // Устанавливаем UTC
            publicKeyUserCertificateService.savePublicKeyUserCertificate(PublicKeyUserCertificate.builder()
                    .fio(fullUserName)
                    .certificate(convertCertificateToPem(certificate))
                    .login(containerInputInfo.getLogin())
                    .originalFilename("certificate.crt")
                    .serialNumber(certificate.getSerialNumber()
                            .toString())
                    .dateStart(formatter.format(certificate.getNotBefore()))
                    .dateEnd(formatter.format(certificate.getNotAfter()))
                    .dateStartPrivateKey(formatter.format(certificate.getNotBefore()))
                    .dateEndPrivateKey(formatter.format(certificate.getNotAfter()))
                    .inn("")
                    .type(TypeUserCertificateEnum.SIMPLE)
                    .issuerWho(certificate.getIssuerDN()
                            .getName())
                    .imprint(getThumbprint(certificate))
                    .build());
        }
        return encryptionKeyRepository.save(keyEntity);
    }
    public static String getThumbprint(X509Certificate certificate){
        // Получаем закодированное представление сертификата
        byte[] encoded = null;
        try {
            encoded = certificate.getEncoded();
        } catch (CertificateEncodingException e) {
            throw new RuntimeException(e);
        }

        // Создаём объект MessageDigest для SHA-1
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        // Вычисляем хэш сертификата
        byte[] hash = digest.digest(encoded);

        // Форматируем хэш в шестнадцатичную строку
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString().toUpperCase(); // Возвращаем отпечаток в верхнем регистре
    }
    public static String convertCertificateToPem(X509Certificate certificate) {
        // Получаем закодированное представление сертификата
        byte[] encoded;
        try {
            encoded = certificate.getEncoded();
        } catch (CertificateEncodingException e) {
            throw new RuntimeException("Ошибка при получении закодированного сертификата", e);
        }

        // Кодируем в Base64
        String base64Encoded = Base64.getEncoder().encodeToString(encoded);

        // Форматируем в PEM
        StringBuilder pem = new StringBuilder();
        pem.append("-----BEGIN CERTIFICATE-----\n");
        pem.append(base64Encoded.replaceAll("(.{64})", "$1\n")); // Добавляем переносы строк каждые 64 символа
        pem.append("-----END CERTIFICATE-----\n");

        return pem.toString();
    }
    private static byte[] createSignature(byte[] data, PrivateKey privateKey, X509Certificate certificate,
                                          String signatureAlgorithm, boolean attached)
            throws IOException, OperatorCreationException, CMSException, CertificateEncodingException {
        CMSProcessableByteArray msg = new CMSProcessableByteArray(data);
        List<X509Certificate> certList = Collections.singletonList(certificate);
        JcaCertStore certs = new JcaCertStore(certList);
        CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
        ContentSigner signer = new JcaContentSignerBuilder(signatureAlgorithm).setProvider("BC")
                .build(privateKey);
        gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().setProvider("BC")
                .build()).build(signer, certificate));
        gen.addCertificates(certs);
        CMSSignedData sigData = gen.generate(msg, attached);
        return sigData.getEncoded();
    }

    private void deleteFileIfExists(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            FileUtils.deleteFile(filePath);
        }
    }

    private PrivateKey getPrivateKey(byte[] privateKeyBytes, String algorithm) {
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(getSimpleSignatureKeyAlgorithm(algorithm));
            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("Error generating private key: ", e);
            return null;
        }
    }

    public PublicKey getPublicKey(byte[] publicKeyBytes, String algorithm) {
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(getSimpleSignatureKeyAlgorithm(algorithm));
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("Error generating public key: ", e);
            return null;
        }
    }

    public static String getSimpleSignatureKeyAlgorithm(String signatureAlgorithm) {
        if (signatureAlgorithm.endsWith("RSA")) {
            return "RSA";
        } else if (signatureAlgorithm.endsWith("ECDSA")) {
            return "EC";
        } else if (signatureAlgorithm.endsWith("DSA")) {
            return "DSA";
        }
        return signatureAlgorithm;
    }

    public KeyPair getKeyPairByEncryptionKeyEntity(EncryptionKeyEntity keyEntity) {
        PrivateKey privateKey = getPrivateKey(keyEntity.getPrivateKey(), keyEntity.getAlgorithm()
                .getAlgorithmName());
        if (privateKey == null) {
            throw new RuntimeException("Private key not found");
        }
        PublicKey publicKey = getPublicKey(keyEntity.getPublicKey(), keyEntity.getAlgorithm()
                .getAlgorithmName());
        if (publicKey == null) {
            throw new RuntimeException("Public key not found");
        }
        return new KeyPair(publicKey, privateKey);
    }

    public X509Certificate createSelfSignedCertificate(KeyPair keyPair,
                                                       SimpleSignatureAlgorithm simpleSignatureAlgorithm,
                                                       String fullUserName) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] serialBytes = new byte[16];
            random.nextBytes(serialBytes);
            long serialNumber = Math.abs(new BigInteger(serialBytes).longValue());

            X500Name issuerAndSubject = new X500Name(sesCertificatesIssuer);
            Calendar calendar = Calendar.getInstance();
            Date notBefore = calendar.getTime();
            calendar.add(Calendar.YEAR, sesCertificatesAge);
            Date notAfter = calendar.getTime();
            X500Name subject = new X500Name("CN=" + fullUserName);
            X509v3CertificateBuilder builder = new X509v3CertificateBuilder(
                    issuerAndSubject,
                    BigInteger.valueOf(serialNumber),
                    notBefore,
                    notAfter,
                    subject,
                    convertToSubjectPublicKeyInfo(keyPair.getPublic()));
            ContentSigner signer = new JcaContentSignerBuilder(simpleSignatureAlgorithm.getAlgorithmName())
                    .build(keyPair.getPrivate());

            X509CertificateHolder holder = builder.build(signer);
            return new JcaX509CertificateConverter().getCertificate(holder);
        } catch (OperatorCreationException | CertificateException e) {
            throw new RuntimeException("Ошибка при создании самоподписанного сертификата", e);
        }
    }

    public static SubjectPublicKeyInfo convertToSubjectPublicKeyInfo(PublicKey publicKey) {
        try {
            AsymmetricKeyParameter params = PublicKeyFactory.createKey(publicKey.getEncoded());
            return SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(params);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при конвертации PublicKey в SubjectPublicKeyInfo", e);
        }
    }

    private static KeyPair generateKeyPair(SimpleSignatureAlgorithm simpleSignatureAlgorithm, KeySize keySize) {
        try {
            String keyAlgorithm = getSimpleSignatureKeyAlgorithm(simpleSignatureAlgorithm.getAlgorithmName());
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(keyAlgorithm);
            keyPairGenerator.initialize(keySize.getSize());
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Ошибка при генерации ключевой пары", e);
        }
    }
}

