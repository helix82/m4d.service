package su.opencode.digsigcik.synapi.service;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.multipart.MultipartFile;
import ru.CryptoPro.CAdES.CAdESSignature;
import ru.CryptoPro.CAdES.CAdESType;
import ru.CryptoPro.CAdES.exception.CAdESException;
import ru.CryptoPro.CAdES.tools.CAdESUtility;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCSP.JCSP;
import su.opencode.digsigcik.synapi.dto.ContainerInputInfoDto;
import su.opencode.digsigcik.synapi.dto.ReportContainerDTO;
import su.opencode.digsigcik.synapi.dto.ReportDTO;
import su.opencode.digsigcik.synapi.enums.SignatureType;
import su.opencode.digsigcik.synapi.util.Constants;
import su.opencode.digsigcik.synapi.util.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.*;


@Service
@Slf4j
public class SignatureServiceImpl implements SignatureService {

    private final ContainerService containerService;
    private final SignatureVerificationService signatureVerificationService;

    public SignatureServiceImpl(ContainerService containerService,
                                @Qualifier("cryptopro-keylab") SignatureVerificationService signatureVerificationService) {
        this.containerService = containerService;
        this.signatureVerificationService = signatureVerificationService;
    }


    @Override
    public Resource createSignatureResource(@NotNull MultipartFile srcFile, @NotNull MultipartFile filePkcs12,
                                            @NotNull String passwordPkcs12,
                                            @NotNull ContainerInputInfoDto containerInputInfo) throws IOException {
        KeyPair keyPair;
        List<X509Certificate> certificates = new ArrayList<>();
        try {
            keyPair = loadKeyPair(filePkcs12, passwordPkcs12, certificates);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при чтении ключей из PKCS#12 файла", e);
        }

        byte[] sign;
        try {
            sign = createSignatureBasedOnType(srcFile, keyPair, certificates, containerInputInfo);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании или проверке подписи", e);
        }
        containerInputInfo.setSignatureAlgorithm(keyPair.getPrivate()
                .getAlgorithm());
        String sigFilePathName = saveSignatureToFile(sign);
        MultipartFile sigMultipartFile = FileUtils.mapFileToMultipart(sigFilePathName);
        deleteFileIfExists(sigFilePathName);

        // Выполнить проверку подписи
        ReportContainerDTO reportContainerDTO;
        try {
            if (containerInputInfo.isSignatureDetached()) {
                reportContainerDTO = signatureVerificationService.verifyDetachedGetPdfReport(srcFile.getBytes(), sign
                        , srcFile.getOriginalFilename(), srcFile.getOriginalFilename() + ".p7s");
            } else {
                reportContainerDTO = signatureVerificationService.verifyAttachedGetPdfReport(sign,
                        srcFile.getOriginalFilename() + ".p7s");
            }
        } catch (ResourceAccessException | IOException | URISyntaxException e) {
            log.warn("Не удалось подключиться к сервису верификации: {}", e.getMessage());
            reportContainerDTO = ReportContainerDTO.builder()
                    .signReport(null)
                    .signCheck(null)
                    .build();
        } catch (Exception e) {
            log.warn("Не удалось подключиться к сервису верификации: {}", e.getMessage());
            reportContainerDTO = ReportContainerDTO.builder()
                    .signReport(null)
                    .signCheck(null)
                    .build();
        }
        containerInputInfo.setSignCheck(reportContainerDTO.getSignCheck());
        containerInputInfo.setSignReport(reportContainerDTO.getSignReport());
        return containerService.createContainer(srcFile, sigMultipartFile, containerInputInfo);
    }

    private KeyPair loadKeyPair(MultipartFile filePkcs12, String passwordPkcs12, List<X509Certificate> certificates) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PFXSTORE", "JCSP");
        keyStore.load(filePkcs12.getInputStream(), passwordPkcs12.toCharArray());

        Enumeration<String> aliases = keyStore.aliases();
        PrivateKey privateKey = null;
        PublicKey publicKey = null;
        X509Certificate certificate;

        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (keyStore.isKeyEntry(alias)) {
                privateKey = (PrivateKey) keyStore.getKey(alias, passwordPkcs12.toCharArray());
                if (privateKey != null) {
                    Certificate[] certificateChain = keyStore.getCertificateChain(alias);
                    if (certificateChain != null) {
                        for (Certificate cert : certificateChain) {
                            certificates.add((X509Certificate) cert);
                        }
                    }
                    certificate = (X509Certificate) keyStore.getCertificate(alias);
                    publicKey = certificate.getPublicKey();
                    log.debug("Alias: " + alias);
                    log.debug("Private Key: " + privateKey);
                    log.debug("Public Key: " + publicKey);
                    log.debug("Certificate: " + certificate);
                }
            }
        }

        return new KeyPair(publicKey, privateKey);
    }

    private byte[] createSignatureBasedOnType(MultipartFile srcFile, KeyPair keyPair,
                                              List<X509Certificate> certificates,
                                              ContainerInputInfoDto containerInputInfo) throws Exception {
        if (containerInputInfo.isSignatureDetached()) {
            return createSignature(srcFile.getBytes(), keyPair.getPrivate(), certificates.get(0), certificates, containerInputInfo.getSignatureType(),false);
        } else {
            return createSignature(srcFile.getBytes(), keyPair.getPrivate(), certificates.get(0), certificates, containerInputInfo.getSignatureType(),true);
        }
    }

    private ReportDTO verifySignature(MultipartFile srcFile, byte[] sign, ContainerInputInfoDto containerInputInfo) throws Exception {
        if (containerInputInfo.isSignatureDetached()) {
            return signatureVerificationService.verifyDetached(srcFile.getBytes(), sign, srcFile.getOriginalFilename(),srcFile.getOriginalFilename()+".p7s");
        } else {
            return signatureVerificationService.verifyAttached(sign, srcFile.getOriginalFilename());
        }
    }

    private String saveSignatureToFile(byte[] sign) {
        String sourceDirectoryPathName = String.format("%s%s", Constants.ROOT_FILE_STORAGE_PATH,
                Constants.SOURCE_DIRECTORY_PATH);
        String sigFilePathName = String.format("%s%s", sourceDirectoryPathName, "temp.p7s");
        FileUtils.createDirectoryIfSheNotExist(sourceDirectoryPathName);
        FileUtils.createFile(sigFilePathName, sign);
        return sigFilePathName;
    }


    public static byte[] createSignature(byte[] data, PrivateKey privateKey, X509Certificate certificate,
                                         List<X509Certificate> certificates, SignatureType signatureType, boolean attached)
            throws IOException, OperatorCreationException, CMSException, CertificateEncodingException, CAdESException {

        if (gostAlgoritm(privateKey.getAlgorithm())) {
            JcaCertStore certs = new JcaCertStore(certificates);
            CAdESUtility.initJCPAlgorithms();
            CAdESSignature cadesSignature = new CAdESSignature(!attached);
            if (signatureType.equals(SignatureType.CAdES_BES)) {
                cadesSignature.addSigner(JCSP.PROVIDER_NAME, keyAlgToDigestOid(privateKey.getAlgorithm()),
                        keyAlgToKeyAlgorithmOid(privateKey.getAlgorithm()), privateKey, certificates, CAdESType.CAdES_BES
                        , null, false);
            } else if (signatureType.equals(SignatureType.CAdES_T)) {
                cadesSignature.addSigner(JCSP.PROVIDER_NAME, keyAlgToDigestOid(privateKey.getAlgorithm()),
                        keyAlgToKeyAlgorithmOid(privateKey.getAlgorithm()), privateKey, certificates, CAdESType.CAdES_T,
                        "http://keylab/tsp/tsp.srf", false);
            } else if (signatureType.equals(SignatureType.CAdES_X_Long_Type_1)) {
                cadesSignature.addSigner(JCSP.PROVIDER_NAME, keyAlgToDigestOid(privateKey.getAlgorithm()),
                        keyAlgToKeyAlgorithmOid(privateKey.getAlgorithm()), privateKey, certificates, CAdESType.CAdES_X_Long_Type_1,
                        "http://keylab/tsp/tsp.srf", false);
            } else if (signatureType.equals(SignatureType.CAdES_A)) {
                cadesSignature.addSigner(JCSP.PROVIDER_NAME, keyAlgToDigestOid(privateKey.getAlgorithm()),
                        keyAlgToKeyAlgorithmOid(privateKey.getAlgorithm()), privateKey, certificates, CAdESType.CAdES_A,
                        "http://keylab/tsp/tsp.srf", false);
            } else if (signatureType.equals(SignatureType.XAdES_BES)) {
            //TODO: IMPLEMENT
            }
            ByteArrayOutputStream signatureStream = new ByteArrayOutputStream();
            cadesSignature.open(signatureStream);
            cadesSignature.update(data);
            cadesSignature.setCertificateStore(certs);
            cadesSignature.close();
            return signatureStream.toByteArray();
        } else {
            CMSProcessableByteArray msg = new CMSProcessableByteArray(data);
            List<X509Certificate> certList = new ArrayList<>(Collections.singletonList(certificate));
            JcaCertStore certs = new JcaCertStore(certList);
            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
            ContentSigner signer =
                    new JcaContentSignerBuilder(privateKey.getAlgorithm()).setProvider("BC")
                            .build(privateKey);
            gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().setProvider("BC")
                    .build()).build(signer, certificate));
            gen.addCertificates(certs);
            CMSSignedData sigData = gen.generate(msg, attached);
            return sigData.getEncoded();
        }
    }


    private void deleteFileIfExists(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            FileUtils.deleteFile(filePath);
        }
    }

    public static boolean gostAlgoritm(String keyAlgName) {
        Set<String> gostAlgorithms = Set.of(
                JCP.GOST_EL_2012_256_NAME,
                JCP.GOST_DH_2012_256_NAME,
                JCP.GOST_PARAMS_SIG_2012_256_KEY_OID,
                JCP.GOST_PARAMS_EXC_2012_256_KEY_OID,
                JCP.GOST_EL_2012_512_NAME,
                JCP.GOST_DH_2012_512_NAME,
                JCP.GOST_PARAMS_SIG_2012_512_KEY_OID,
                JCP.GOST_PARAMS_EXC_2012_512_KEY_OID
        );
        return gostAlgorithms.contains(keyAlgName.toUpperCase());
    }

    public static String keyAlgToDigestOid(String keyAlgName) {
        Map<String, String> digestOidMap = Map.of(
                JCP.GOST_EL_2012_256_NAME, JCP.GOST_DIGEST_2012_256_OID,
                JCP.GOST_DH_2012_256_NAME, JCP.GOST_DIGEST_2012_256_OID,
                JCP.GOST_PARAMS_SIG_2012_256_KEY_OID, JCP.GOST_DIGEST_2012_256_OID,
                JCP.GOST_PARAMS_EXC_2012_256_KEY_OID, JCP.GOST_DIGEST_2012_256_OID,
                JCP.GOST_EL_2012_512_NAME, JCP.GOST_DIGEST_2012_512_OID,
                JCP.GOST_DH_2012_512_NAME, JCP.GOST_DIGEST_2012_512_OID,
                JCP.GOST_PARAMS_SIG_2012_512_KEY_OID, JCP.GOST_DIGEST_2012_512_OID,
                JCP.GOST_PARAMS_EXC_2012_512_KEY_OID, JCP.GOST_DIGEST_2012_512_OID
        );

        return digestOidMap.getOrDefault(keyAlgName.toUpperCase(), JCP.GOST_DIGEST_OID);
    }

    public static String keyAlgToKeyAlgorithmOid(String keyAlgName) {
        Map<String, String> keyAlgOidMap = Map.of(
                JCP.GOST_EL_2012_256_NAME, JCP.GOST_PARAMS_SIG_2012_256_KEY_OID,
                JCP.GOST_DH_2012_256_NAME, JCP.GOST_PARAMS_SIG_2012_256_KEY_OID,
                JCP.GOST_EL_2012_512_NAME, JCP.GOST_PARAMS_SIG_2012_512_KEY_OID,
                JCP.GOST_DH_2012_512_NAME, JCP.GOST_PARAMS_SIG_2012_512_KEY_OID
        );

        return keyAlgOidMap.getOrDefault(keyAlgName.toUpperCase(), JCP.GOST_EL_KEY_OID);
    }

}
