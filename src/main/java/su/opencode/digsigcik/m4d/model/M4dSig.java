package su.opencode.digsigcik.m4d.model;

import lombok.Data;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import su.opencode.digsigcik.m4d.util.Base58;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Security;

@Data
public class M4dSig {
    private String status;
    private String issuerPublicKey;
    private String issuerSnilsHash;
    private String issuerOgrnHash;

    // Конструктор по умолчанию
    public M4dSig() {}

    // Конструктор с параметрами
    public M4dSig(String status, String issuerPublicKey, String issuerSnilsHash, String issuerOgrnHash) {
        this.status = status;
        this.issuerPublicKey = issuerPublicKey;
        this.issuerSnilsHash = issuerSnilsHash;
        this.issuerOgrnHash = issuerOgrnHash;
    }

    // Геттеры и сеттеры
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIssuerPublicKey() {
        return issuerPublicKey;
    }

    public void setIssuerPublicKey(String issuerPublicKey) {
        this.issuerPublicKey = issuerPublicKey;
    }

    public String getIssuerSnilsHash() {
        return issuerSnilsHash;
    }

    public void setIssuerSnilsHash(String issuerSnilsHash) throws Exception {
        this.issuerSnilsHash = getHash(issuerSnilsHash);
    }

    public String getIssuerOgrnHash() {
        return issuerOgrnHash;
    }

    public void setIssuerOgrnHash(String issuerOgrnHash) throws Exception {
        this.issuerOgrnHash = getHash(issuerOgrnHash);
    }

    private static String getHash(String origin) throws Exception {
        byte[] ogrnByte = origin.getBytes();
        String base64Encoded = Base64.toBase64String(ogrnByte);
        String saltedBase64 = '"' + base64Encoded + '"';
        byte[] hash = hashGOST3411_2012_256(saltedBase64.getBytes(StandardCharsets.UTF_8));
        return Base58.encode(hash);
    }

    private static byte[] hashGOST3411_2012_256(byte[] input) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        MessageDigest digest = MessageDigest.getInstance("GOST3411-2012-256");
        return digest.digest(input);
    }

    private  static String hexConverter(String origin) {
        // Преобразуем строку в массив байтов
        byte[] byteArray = hexStringToByteArray(origin);

        // Генерируем результирующую строку
        String result = generateHexOutput(byteArray);
        return result;
    }

    public static byte[] hexStringToByteArray(String s) {
        String[] hexArray = s.split(" ");
        byte[] byteArray = new byte[hexArray.length];
        for (int i = 0; i < hexArray.length; i++) {
            byteArray[i] = (byte) Integer.parseInt(hexArray[i], 16);
        }
        return byteArray;
    }

    public static String generateHexOutput(byte[] bytes) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        M4dSig m4dSig = (M4dSig) o;

        if (!status.equals(m4dSig.status)) return false;
        if (!issuerPublicKey.equals(m4dSig.issuerPublicKey)) return false;
        if (!issuerSnilsHash.equals(m4dSig.issuerSnilsHash)) return false;
        return issuerOgrnHash.equals(m4dSig.issuerOgrnHash);
    }
}
