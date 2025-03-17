package su.opencode.digsigcik.m4d.util;

import lombok.experimental.UtilityClass;
import org.bitcoinj.core.Base58;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

@UtilityClass
public class HashUtils {
    @Value("${m4d.soul}")
    private static String M4D_SOUL;
    public static String getHash(byte[] origin) throws NoSuchAlgorithmException {
        String base64Encoded = Base64.toBase64String(origin);
        //String saltedBase64 = M4D_SOUL + base64Encoded + M4D_SOUL;
        String saltedBase64 = '"' + base64Encoded + '"';
        byte[] hash = hashGOST3411_2012_256(saltedBase64.getBytes(StandardCharsets.UTF_8));
        return Base58.encode(hash);
    }

    private static byte[] hashGOST3411_2012_256(byte[] input) throws NoSuchAlgorithmException {
        Security.addProvider(new BouncyCastleProvider());
        MessageDigest digest = MessageDigest.getInstance("GOST3411-2012-256");
        return digest.digest(input);
    }

    private  static String hexConverter(String origin) {
        // Преобразуем строку в массив байтов
        byte[] byteArray = hexStringToByteArray(origin);

        // Генерируем результирующую строку
        return generateHexOutput(byteArray);
    }

    private static byte[] hexStringToByteArray(String s) {
        String[] hexArray = s.split(" ");
        byte[] byteArray = new byte[hexArray.length];
        for (int i = 0; i < hexArray.length; i++) {
            byteArray[i] = (byte) Integer.parseInt(hexArray[i], 16);
        }
        return byteArray;
    }

    private static String generateHexOutput(byte[] bytes) {
        StringBuilder hexOutput = new StringBuilder();

        // Добавляем фиксированные префиксы
        hexOutput.append("3066301F06082A85030701010101301306072A85030202240006082A8503070101020203430004");

        // Добавляем байты в шестнадцатеричном формате
        for (byte b : bytes) {
            String hex = String.format("%02X", b); // Преобразуем байт в шестнадцатеричное значение
            hexOutput.append(hex);
        }

        return hexOutput.toString();
    }
}
