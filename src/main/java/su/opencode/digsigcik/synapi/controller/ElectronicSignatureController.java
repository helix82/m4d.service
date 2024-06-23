package su.opencode.digsigcik.synapi.controller;

import org.springframework.web.bind.annotation.*;
import su.opencode.digsigcik.synapi.enums.KeySize;
import su.opencode.digsigcik.synapi.enums.SignatureAlgorithm;
import su.opencode.digsigcik.synapi.model.ESSUser;
import su.opencode.digsigcik.synapi.model.EncryptionKeyEntity;
import su.opencode.digsigcik.synapi.model.PinCode;
import su.opencode.digsigcik.synapi.repository.ESUserRepository;
import su.opencode.digsigcik.synapi.repository.EncryptionKeyRepository;
import su.opencode.digsigcik.synapi.repository.PinCodeRepository;
import su.opencode.digsigcik.synapi.service.ElectronicSignatureService;
import su.opencode.digsigcik.synapi.service.PinCodeService;

import java.security.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/signature")
public class ElectronicSignatureController {
    private final EncryptionKeyRepository encryptionKeyRepository;
    private final ElectronicSignatureService signatureService;
    private final PinCodeService pinCodeService;
    private final ESUserRepository esUserRepository;
    private final PinCodeRepository pinCodeRepository;

    public ElectronicSignatureController(EncryptionKeyRepository encryptionKeyRepository,
                                         ElectronicSignatureService signatureService, PinCodeService pinCodeService, ESUserRepository esUserRepository, PinCodeRepository pinCodeRepository) {
        this.encryptionKeyRepository = encryptionKeyRepository;
        this.signatureService = signatureService;
        this.pinCodeService = pinCodeService;
        this.esUserRepository = esUserRepository;
        this.pinCodeRepository = pinCodeRepository;
    }

    @PostMapping("/")
    public EncryptionKeyEntity createEncryptionKey(SignatureAlgorithm algorithm, KeySize keySize, String login) {
        ESSUser essUser = esUserRepository.findESSUserByLogin(login);
        PinCode pinCode = pinCodeRepository.getPinCodeByUser(essUser);
        // Генерация ключевой пары
        try {
            KeyPair keyPair = generateKeyPair(algorithm,keySize);

            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();
            EncryptionKeyEntity keyEntity = new EncryptionKeyEntity();
            keyEntity.setAlgorithm(algorithm);
            keyEntity.setPrivateKey(privateKey.getEncoded());
            keyEntity.setPublicKey(publicKey.getEncoded());
            keyEntity.setPinCode(pinCode);
            return encryptionKeyRepository.save(keyEntity);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    @PutMapping("/")
    public EncryptionKeyEntity updateEncryptionKey(Long id, SignatureAlgorithm algorithm, byte[] privateKey,
                                                   byte[] publicKey, Long pinCodeId) {
        Optional<EncryptionKeyEntity> keyEntity = encryptionKeyRepository.findById(id);
        if (keyEntity.isPresent()) {
            PinCode pinCode = pinCodeService.getPinCodeById(pinCodeId);
            keyEntity.get()
                    .setAlgorithm(algorithm);
            keyEntity.get()
                    .setPrivateKey(privateKey);
            keyEntity.get()
                    .setPublicKey(publicKey);
            keyEntity.get()
                    .setPinCode(pinCode);
            return encryptionKeyRepository.save(keyEntity.get());
        }
        return null;
    }



    @DeleteMapping("/")
    public boolean deleteEncryptionKey(Long id) {
        Optional<EncryptionKeyEntity> keyEntity = encryptionKeyRepository.findById(id);
        if (keyEntity.isPresent()) {
            encryptionKeyRepository.delete(keyEntity.get());
            return true;
        }
        return false;
    }

    @PostMapping("/sign")
    public boolean signWithPinCode(Long id, byte[] fileContent, Long pinCodeId) {
        Optional<EncryptionKeyEntity> keyEntity = encryptionKeyRepository.findById(id);
        if (keyEntity.isEmpty()) {
            return false;
        } else {
            //PinCode pinCode = pinCodeService.getPinCodeById(pinCodeId);
            PrivateKey privateKey = signatureService.getPrivateKey(keyEntity.get()
                    .getPrivateKey(), keyEntity.get()
                    .getPinCode().getPinCode(), keyEntity.get()
                    .getAlgorithm()
                    .getAlgorithmName());
            if (privateKey == null) {
                return false;
            }

            byte[] signature = signatureService.createSignature(fileContent, privateKey);
            return signature != null;
        }
    }

    @PostMapping("/verify")
    public boolean verifySignature(Long id, byte[] fileContent, byte[] signature) {
        Optional<EncryptionKeyEntity> keyEntity = encryptionKeyRepository.findById(id);
        if (keyEntity.isEmpty()) {
            return false;
        } else {

            PublicKey publicKey = signatureService.getPublicKey(keyEntity.get()
                    .getPublicKey(), keyEntity.get()
                    .getAlgorithm()
                    .getAlgorithmName());
            if (publicKey == null) {
                return false;
            }

            return signatureService.verifySignature(fileContent, signature, publicKey);
        }
    }

    private static KeyPair generateKeyPair(SignatureAlgorithm signatureAlgorithm, KeySize keySize) throws NoSuchAlgorithmException {
        String keyAlgorithm = signatureAlgorithm.getAlgorithmName();
        if (keyAlgorithm.endsWith("RSA")) {
            keyAlgorithm = "RSA";
        } else if (keyAlgorithm.endsWith("ECDSA")) {
            keyAlgorithm = "EC";
        } else if (keyAlgorithm.endsWith("DSA")) {
            keyAlgorithm = "DSA";
        }

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(keyAlgorithm);
        keyPairGenerator.initialize(keySize.getSize());
        return keyPairGenerator.generateKeyPair();
    }
}