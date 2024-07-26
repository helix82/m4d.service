package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.GOST3410PrivateKeyParameters;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory;
import org.bouncycastle.jcajce.provider.asymmetric.gost.BCGOST3410PrivateKey;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.CryptoPro.JCP.JCP;
import su.opencode.digsigcik.synapi.enums.KeySize;
import su.opencode.digsigcik.synapi.enums.SignatureAlgorithm;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.model.EncryptionKeyEntity;
import su.opencode.digsigcik.synapi.model.UserProjection;
import su.opencode.digsigcik.synapi.repository.EncryptionKeyRepository;
import su.opencode.digsigcik.synapi.repository.UserProjectionRepository;

import javax.transaction.Transactional;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

import static su.opencode.digsigcik.synapi.util.Constants.*;

@RestController
@RequestMapping("/api/v1/signature")
@AllArgsConstructor
public class EncryptionKeyController {
    private final EncryptionKeyRepository encryptionKeyRepository;
    private final UserProjectionRepository userProjectionRepository;


    @Operation(summary = CONTROLLER_ENCRYPTION_KEY_CREATE,
            description = CONTROLLER_ENCRYPTION_KEY_CREATE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 400, message = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PostMapping("/")
    public EncryptionKeyEntity createEncryptionKey( @RequestParam String login) {
        return createEncryptionKeyWithCustomSignature(SignatureAlgorithm.SHA256withRSA, KeySize.BIT_512, login);

    }

    @Operation(summary = CONTROLLER_ENCRYPTION_KEY_CREATE_WITH_CUSTOM_ALG,
            description = CONTROLLER_ENCRYPTION_KEY_CREATE_WITH_CUSTOM_ALG)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 400, message = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PostMapping("/customize")
    public EncryptionKeyEntity createEncryptionKeyWithCustomSignature(SignatureAlgorithm algorithm, KeySize keySize,
                                                                      @RequestParam String login) {

        if (login == null || login.trim()
                .isEmpty()) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }
        UserProjection userProjection = userProjectionRepository.getUserProjectionByLogin(login);

        if (userProjection == null) {
            userProjection = userProjectionRepository.save(UserProjection.builder()
                    .login(login)
                    .build());
        }

        // Генерация ключевой пары
        try {
            KeyPair keyPair = generateKeyPair(algorithm, keySize);
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();
            EncryptionKeyEntity keyEntity = new EncryptionKeyEntity();
            keyEntity.setAlgorithm(algorithm);
            keyEntity.setPrivateKey(privateKey.getEncoded());
            keyEntity.setPublicKey(publicKey.getEncoded());
            keyEntity.setUserProjection(userProjection);
            return encryptionKeyRepository.save(keyEntity);
        } catch (NoSuchProviderException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Operation(summary = CONTROLLER_ENCRYPTION_KEY_DELETE,
            description = CONTROLLER_ENCRYPTION_KEY_DELETE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 400, message = API_RESPONSE_CODE_400_MESSAGE)
    })
    @DeleteMapping(value = "/", produces = "application/json; charset=UTF-8")
    @Transactional
    public ResponseEntity<?> deleteEncryptionKey(@RequestParam String login) {
        if (login == null || login.trim()
                .isEmpty()) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }
        UserProjection userProjection = userProjectionRepository.getUserProjectionByLogin(login);
        if (userProjection != null) {
            EncryptionKeyEntity keyEntity =
                    encryptionKeyRepository.getEncryptionKeyEntitiesByUserProjection(userProjection);
            if (keyEntity != null) {
                encryptionKeyRepository.deleteEncryptionKeyEntitiesByUserProjection(userProjection);
                return new ResponseEntity<>("", HttpStatus.OK);
            }
        }
        throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
    }


    private static KeyPair generateKeyPair(SignatureAlgorithm signatureAlgorithm, KeySize keySize) throws NoSuchAlgorithmException, NoSuchProviderException {
        String keyAlgorithm = signatureAlgorithm.getAlgorithmName();
        if (keyAlgorithm.endsWith("RSA")) {
            keyAlgorithm = "RSA";
        } else if (keyAlgorithm.endsWith("ECDSA")) {
            keyAlgorithm = "EC";
        } else if (keyAlgorithm.endsWith("DSA")) {
            keyAlgorithm = "DSA";
        } else if (keyAlgorithm.startsWith("GOST3411_2012_256")) {
            keyAlgorithm = JCP.GOST_EL_2012_256_NAME;
        } else if (keyAlgorithm.startsWith("GOST3411_2012_512")) {
            keyAlgorithm = JCP.GOST_EL_2012_512_NAME;
        }

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(keyAlgorithm);
        keyPairGenerator.initialize(keySize.getSize());
        return keyPairGenerator.generateKeyPair();
    }

    public static void writeCertificateToFile(X509Certificate certificate, String filePath) {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            // Записываем сертификат в файл
            fos.write(certificate.getEncoded());
            System.out.println("Сертификат успешно записан в файл: " + filePath);
        } catch (IOException | CertificateEncodingException e) {
            throw new RuntimeException("Ошибка при записи сертификата в файл", e);
        }
    }

    public static X509Certificate createSelfSignedCertificate(KeyPair keyPair) {
        try {
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
            // Строим сертификат
            X509v3CertificateBuilder builder = new X509v3CertificateBuilder(
                    issuerAndSubject,
                    BigInteger.valueOf(serialNumber),
                    notBefore,
                    notAfter,
                    issuerAndSubject,
                    convertToSubjectPublicKeyInfo(keyPair.getPublic()));

            // Подписываем сертификат
            ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSAEncryption")
                    .build(keyPair.getPrivate());
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

    public static void saveKeyPairToPkcs12(KeyPair keyPair, String filePath, String password) {
        try {
            X509Certificate cert = createSelfSignedCertificate(keyPair);
            // --- create a new pkcs12 key store in memory
            KeyStore pkcs12 = KeyStore.getInstance("PKCS12");
            pkcs12.load(null, null);

            // --- create entry in PKCS12
            pkcs12.setKeyEntry("privatekeyalias", keyPair.getPrivate(), password.toCharArray(),
                    new X509Certificate[]{cert});

            // --- store PKCS#12 as file
            try (FileOutputStream p12 = new FileOutputStream(filePath)) {
                pkcs12.store(p12, password.toCharArray());
            }

            System.out.println("Закрытый и открытый ключи успешно сохранены в файл: " + filePath);
        } catch (IOException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new RuntimeException("Ошибка при сохранении ключей в PKCS12 PFX-контейнер", e);
        } catch (CertificateEncodingException e) {
            throw new RuntimeException(e);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }
    }




    public static void saveGostPrivateKeyToFile(PrivateKey privateKey, String filePath) {
        try (FileOutputStream fos = new FileOutputStream(new File(filePath))) {
            // Проверяем, что ключ является GOST-ключом
            //          if (privateKey instanceof BCGOST3410PrivateKey) {
            BCGOST3410PrivateKey gostPrivateKey = (BCGOST3410PrivateKey) privateKey;

            // Получаем параметры GOST-ключа
            GOST3410PrivateKeyParameters keyParams = (GOST3410PrivateKeyParameters) gostPrivateKey.getParameters();
            byte[] keyBytes = keyParams.getX()
                    .toByteArray();

            // Создаем PrivateKeyInfo с GOST-ключом
            PrivateKeyInfo privateKeyInfo = new PrivateKeyInfo(
                    new AlgorithmIdentifier(CryptoProObjectIdentifiers.gostR3410_94),
                    new DEROctetString(keyBytes)
            );

            // Записываем PrivateKeyInfo в файл в формате PEM
            PemObject pemObject = new PemObject("PRIVATE KEY", privateKeyInfo.getEncoded());
            try (JcaPEMWriter pemWriter = new JcaPEMWriter(new java.io.OutputStreamWriter(fos))) {
                pemWriter.writeObject(pemObject);
            }
//            } else {
//                throw new IllegalArgumentException("Переданный ключ не является GOST-ключом");
//            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при записи GOST-ключа в файл", e);
        }
    }
}