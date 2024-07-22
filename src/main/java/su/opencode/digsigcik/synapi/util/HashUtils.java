package su.opencode.digsigcik.synapi.util;

import lombok.experimental.UtilityClass;
import org.bouncycastle.util.encoders.Hex;
import su.opencode.digsigcik.synapi.enums.HashAlgorithm;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

@UtilityClass
public class HashUtils {
    public static String generateHash(byte[] bytes, HashAlgorithm hashAlgorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(hashAlgorithm.getAlgorithmName());
            byte[] hash = md.digest(bytes);
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(String.format("Алгоритм %s не поддерживается", hashAlgorithm.getAlgorithmName()));
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static String calculateSignatureDocumentHash(byte[] documentBytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256", "BC");
            byte[] hash = md.digest(documentBytes);
            return Hex.toHexString(hash);
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Error calculating signature document hash", e);
        }
    }
}
