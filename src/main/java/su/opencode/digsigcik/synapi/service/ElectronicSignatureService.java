package su.opencode.digsigcik.synapi.service;

import org.springframework.stereotype.Service;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Service
public class ElectronicSignatureService {
    public PrivateKey getPrivateKey(byte[] privateKeyBytes, String pinCode, String algorithm) {
        try {
            byte[] decryptedPrivateKeyBytes = decryptPrivateKey(privateKeyBytes, pinCode, algorithm);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decryptedPrivateKeyBytes);
            String keyAlgorithm = algorithm;
            if (keyAlgorithm.endsWith("RSA")) {
                keyAlgorithm = "RSA";
            } else if (keyAlgorithm.endsWith("ECDSA")) {
                keyAlgorithm = "EC";
            } else if (keyAlgorithm.endsWith("DSA")) {
                keyAlgorithm = "DSA";
            }

            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(keyAlgorithm);
            KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
    }

    public PublicKey getPublicKey(byte[] publicKeyBytes, String algorithm) {
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            String keyAlgorithm = algorithm;
            if (keyAlgorithm.endsWith("RSA")) {
                keyAlgorithm = "RSA";
            } else if (keyAlgorithm.endsWith("ECDSA")) {
                keyAlgorithm = "EC";
            } else if (keyAlgorithm.endsWith("DSA")) {
                keyAlgorithm = "DSA";
            }
            KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] createSignature(byte[] fileContent, PrivateKey privateKey) {
        try {
            Signature signature = Signature.getInstance("SHA256with" + privateKey.getAlgorithm());
            signature.initSign(privateKey);
            signature.update(fileContent);
            return signature.sign();
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean verifySignature(byte[] fileContent, byte[] signatureBytes, PublicKey publicKey) {
        try {
            Signature verificationSignature = Signature.getInstance("SHA256with" + publicKey.getAlgorithm());
            verificationSignature.initVerify(publicKey);
            verificationSignature.update(fileContent);
            return verificationSignature.verify(signatureBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
            return false;
        }
    }

    private byte[] decryptPrivateKey(byte[] privateKeyBytes, String pinCode, String algorithm) throws InvalidKeyException {
        // Implement private key decryption using the provided pin code and algorithm
        return privateKeyBytes;
    }
}