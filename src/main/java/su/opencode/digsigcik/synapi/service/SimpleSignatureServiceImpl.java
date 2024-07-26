package su.opencode.digsigcik.synapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.DocumentException;
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
import org.bouncycastle.jcajce.spec.GOST3410ParameterSpec;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.dto.ContainerInputInfoDto;
import su.opencode.digsigcik.synapi.dto.ReportDTO;
import su.opencode.digsigcik.synapi.enums.KeySize;
import su.opencode.digsigcik.synapi.enums.SignatureAlgorithm;
import su.opencode.digsigcik.synapi.model.EncryptionKeyEntity;
import su.opencode.digsigcik.synapi.repository.EncryptionKeyRepository;
import su.opencode.digsigcik.synapi.util.Constants;
import su.opencode.digsigcik.synapi.util.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

import static su.opencode.digsigcik.synapi.util.ReportUtils.createPdf;


@Service

public class SimpleSignatureServiceImpl implements SimpleSignatureService {

    private final ContainerService containerService;
    private final EncryptionKeyRepository encryptionKeyRepository;
    private final SignatureVerificationService signatureVerificationService;

    public SimpleSignatureServiceImpl(ContainerService containerService,
                                      EncryptionKeyRepository encryptionKeyRepository,
                                      @Qualifier("bouncy-castle") SignatureVerificationService signatureVerificationService) {
        this.containerService = containerService;
        this.encryptionKeyRepository = encryptionKeyRepository;
        this.signatureVerificationService = signatureVerificationService;
    }

    @Override
    public Resource createSimpleSignatureResource(@NotNull MultipartFile srcFile,
                                                  @NotNull ContainerInputInfoDto containerInputInfo) throws IOException {


        X509Certificate certificate;
        String fullUserName = containerInputInfo.getLastname() + " " +
                containerInputInfo.getFirstname() + " " +
                containerInputInfo.getPatronymic();
        EncryptionKeyEntity keyEntity =
                encryptionKeyRepository.getEncryptionKeyEntitiesByUserProjection(containerInputInfo.getUserProjection());
        if (keyEntity == null) {
            // Генерация ключевой пары
            try {
                SignatureAlgorithm algorithm =
                        SignatureAlgorithm.fromAlgorithmName(containerInputInfo.getSignatureAlgorithm());
                KeyPair keyPair = generateKeyPair(algorithm, containerInputInfo.getKeySize());
                PrivateKey privateKey = keyPair.getPrivate();
                PublicKey publicKey = keyPair.getPublic();
                keyEntity = new EncryptionKeyEntity();
                keyEntity.setAlgorithm(algorithm);
                keyEntity.setPrivateKey(privateKey.getEncoded());
                keyEntity.setPublicKey(publicKey.getEncoded());
                keyEntity.setUserProjection(containerInputInfo.getUserProjection());
                certificate = createSelfSignedCertificate(keyPair, algorithm, fullUserName);
                keyEntity.setCert(certificate.getEncoded());
                keyEntity = encryptionKeyRepository.save(keyEntity);
            } catch (CertificateEncodingException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(keyEntity.getCert()));
            } catch (CertificateException e) {
                throw new RuntimeException(e);
            }
        }
        KeyPair keyPair = getKeyPairByEncryptionKeyEntity(keyEntity);

        byte[] sign;
        ReportDTO reportDTO;
        try {
            if (containerInputInfo.isSignatureDetached()) {
                sign = createSignature(srcFile.getBytes(), keyPair
                        .getPrivate(), certificate, containerInputInfo.getSignatureAlgorithm(), false);
                reportDTO = signatureVerificationService.verifyDetached(srcFile.getBytes(), sign,
                        srcFile.getOriginalFilename());
            } else {
                sign = createSignature(srcFile.getBytes(), keyPair
                        .getPrivate(), certificate, containerInputInfo.getSignatureAlgorithm(), true);
                reportDTO = signatureVerificationService.verifyAttached(sign, srcFile.getOriginalFilename());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String sourceDirectoryPathName = String.format("%s%s", Constants.ROOT_FILE_STORAGE_PATH,
                Constants.SOURCE_DIRECTORY_PATH);
        String sigFilePathName = String.format("%s%s", sourceDirectoryPathName, "temp.p7s");
        FileUtils.createDirectoryIfSheNotExist(sourceDirectoryPathName);
        FileUtils.createFile(sigFilePathName, sign);

        MultipartFile sigMultipartFile = FileUtils.mapFileToMultipart(sigFilePathName);
        deleteFileIfExists(sigFilePathName);

        ObjectMapper objectMapper = new ObjectMapper();
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

    public static byte[] createSignature(byte[] data, PrivateKey privateKey, X509Certificate certificate,
                                         String signatureAlgorithm, boolean attached)
            throws IOException, OperatorCreationException, CMSException, CertificateEncodingException {
        CMSProcessableByteArray msg = new CMSProcessableByteArray(data);
        List<X509Certificate> certList = new ArrayList<>(Collections.singletonList(certificate));
        Store<JcaCertStore> certs = new JcaCertStore(certList);
        CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
        ContentSigner signer =
                new org.bouncycastle.operator.jcajce.JcaContentSignerBuilder(signatureAlgorithm).setProvider("BC")
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
            byte[] decryptedPrivateKeyBytes = decryptPrivateKey(privateKeyBytes, algorithm);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decryptedPrivateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(getKeyAlgorithm(algorithm));
            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
    }


    public PublicKey getPublicKey(byte[] publicKeyBytes, String algorithm) {
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(getKeyAlgorithm(algorithm));
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getKeyAlgorithm(String signatureAlgorithm) {
        if (signatureAlgorithm.endsWith("RSA")) {
            return "RSA";
        } else if (signatureAlgorithm.endsWith("ECDSA")) {
            return "EC";
        } else if (signatureAlgorithm.endsWith("DSA")) {
            return "DSA";
        } else if (signatureAlgorithm.startsWith("GOST3411WITHECGOST3410-2012")) {
            return "ECGOST3410-2012";
        }
        return signatureAlgorithm;
    }

    private byte[] decryptPrivateKey(byte[] privateKeyBytes, String algorithm) throws InvalidKeyException {
        // Implement private key decryption using the provided pin code and algorithm
        return privateKeyBytes;
    }


    public KeyPair getKeyPairByEncryptionKeyEntity(EncryptionKeyEntity keyEntity) {
        PrivateKey privateKey = getPrivateKey(keyEntity
                .getPrivateKey(), keyEntity
                .getAlgorithm()
                .getAlgorithmName());

        if (privateKey == null) {
            throw new RuntimeException("privateKey not found");
        }
        PublicKey publicKey = getPublicKey(keyEntity
                .getPublicKey(), keyEntity
                .getAlgorithm()
                .getAlgorithmName());
        if (publicKey == null) {
            throw new RuntimeException("publicKey not found");
        }
        return new KeyPair(publicKey, privateKey);
    }

    public X509Certificate createSelfSignedCertificate(KeyPair keyPair, SignatureAlgorithm signatureAlgorithm, String fullUserName) {
        try {
            // KeyPair keyPair = readKeyPairFromPkcs12("oper.pfx","1234");
            // Генерируем серийный номер сертификата
            SecureRandom random = new SecureRandom();
            byte[] serialBytes = new byte[16];
            random.nextBytes(serialBytes);
            long serialNumber = Math.abs(new BigInteger(serialBytes).longValue());

            // Устанавливаем данные владельца и издателя сертификата
            X500Name issuerAndSubject = new X500Name("CN=SEP Self-Signed Certificate");

            // Устанавливаем срок действия сертификата
            Calendar calendar = Calendar.getInstance();
            Date notBefore = calendar.getTime();
            calendar.add(Calendar.YEAR, 1);
            Date notAfter = calendar.getTime();
            X500Name subject = new X500Name("CN="+fullUserName);
            // Строим сертификат
            X509v3CertificateBuilder builder = new X509v3CertificateBuilder(
                    issuerAndSubject,
                    BigInteger.valueOf(serialNumber),
                    notBefore,
                    notAfter,
                    subject,
                    convertToSubjectPublicKeyInfo(keyPair.getPublic()));

            // Подписываем сертификат
            ContentSigner signer;
            if (signatureAlgorithm.getAlgorithmName()
                    .startsWith("GOST3411WITHECGOST3410-2012")) {
                signer = new JcaContentSignerBuilder(signatureAlgorithm.getAlgorithmName())
                        .setProvider("BC")
                        .build(keyPair.getPrivate());
            } else {
                signer = new JcaContentSignerBuilder(signatureAlgorithm.getAlgorithmName())
                        .build(keyPair.getPrivate());
            }
            X509CertificateHolder holder = builder.build(signer);

            // Конвертируем сертификат в формат X509Certificate
            return new JcaX509CertificateConverter().getCertificate(holder);
        } catch (OperatorCreationException | CertificateException e) {
            throw new RuntimeException("Ошибка при создании самоподписанного сертификата", e);
        }
    }

    public static SubjectPublicKeyInfo convertToSubjectPublicKeyInfo(PublicKey publicKey) {
        try {
            AsymmetricKeyParameter params = PublicKeyFactory.createKey(publicKey.getEncoded());
            // Создаем SubjectPublicKeyInfo
            return SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(params);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при конвертации PublicKey в SubjectPublicKeyInfo", e);
        }
    }

    public static KeyPair readKeyPairFromPkcs12(String filePath, String password) {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            // Загружаем PKCS#12 хранилище
            KeyStore keyStore = null;
            try {
                keyStore = KeyStore.getInstance("PKCS12", "BC");
            } catch (NoSuchProviderException e) {
                throw new RuntimeException(e);
            }
            keyStore.load(fis, password.toCharArray());

            // Находим первый доступный сертификат и извлекаем соответствующие ключи
            String alias = keyStore.aliases()
                    .nextElement();
            Key key = keyStore.getKey(alias, password.toCharArray());
            if (key instanceof PrivateKey) {
                // Get certificate of public key
                Certificate cert = keyStore.getCertificate(alias);

                // Get public key
                PublicKey publicKey = cert.getPublicKey();

                // Return a key pair
                return new KeyPair(publicKey, (PrivateKey) key);
            }
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException |
                 UnrecoverableEntryException e) {
            throw new RuntimeException("Ошибка при чтении ключей из PKCS#12 файла", e);
        }
        return null;
    }

    private static KeyPair generateKeyPair(SignatureAlgorithm signatureAlgorithm, KeySize keySize) throws NoSuchAlgorithmException {
        //TODO добавить реализацию генератора для КриптоПро
        String keyAlgorithm = signatureAlgorithm.getAlgorithmName();
        if (keyAlgorithm.endsWith("RSA")) {
            keyAlgorithm = "RSA";
        } else if (keyAlgorithm.endsWith("ECDSA")) {
            keyAlgorithm = "EC";
        } else if (keyAlgorithm.endsWith("DSA")) {
            keyAlgorithm = "DSA";
        } else if (keyAlgorithm.startsWith("GOST3411WITHECGOST3410-2012")) {
            // Для алгоритмов ГОСТ используем Bouncy Castle
            KeyPairGenerator keyPairGenerator = null;
            try {
                keyPairGenerator = KeyPairGenerator.getInstance("ECGOST3410-2012", "BC");
            } catch (NoSuchProviderException e) {
                throw new RuntimeException(e);
            }
            try {
                keyPairGenerator.initialize(new GOST3410ParameterSpec("GostR3410-2001-CryptoPro-A"));
            } catch (InvalidAlgorithmParameterException e) {
                throw new RuntimeException(e);
            }
            return keyPairGenerator.generateKeyPair();
        }
        // Для всех остальных алгоритмов используем стандартный генератор пар ключей
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(keyAlgorithm);
        keyPairGenerator.initialize(keySize.getSize());
        return keyPairGenerator.generateKeyPair();
    }
}
