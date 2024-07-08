package example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

public class FileSignature2 {
    private static String PIN_CODE = "erterer";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    public static void main(String[] args) {
        try {
            // Генерация ключевой пары
            KeyPair keyPair = generateKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();

            // Создание подписи
            byte[] fileContent = "Hello, World!".getBytes(StandardCharsets.UTF_8);
            byte[] signature = createSignature(fileContent, privateKey);

            // Проверка подписи
            boolean isValid = verifySignature(fileContent, signature, publicKey);
            System.out.println(Base64.getEncoder().encodeToString(signature));
            System.out.println("Signature is valid: " + isValid);
            PIN_CODE = "4321";
            isValid = verifySignature(fileContent, signature, publicKey);
            System.out.println(Base64.getEncoder().encodeToString(signature));
            System.out.println("Signature is valid: " + isValid);

        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | IOException e) {
            e.printStackTrace();
        }
    }

    private static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }

    private static byte[] createSignature(byte[] fileContent, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        //signature.initSign(privateKey);
        signature.initSign(privateKey, new SecureRandom(PIN_CODE.getBytes()));
        // Добавление пин-кода и текущего времени
        //String pinCodeAndTime = PIN_CODE + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
//        String pinCodeAndTime = PIN_CODE;
//        signature.update(pinCodeAndTime.getBytes(StandardCharsets.UTF_8));
        signature.update(fileContent);

        return signature.sign();
    }

    private static boolean verifySignature(byte[] fileContent, byte[] signature, PublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {
        Signature verificationSignature = Signature.getInstance(SIGNATURE_ALGORITHM);
        verificationSignature.initVerify(publicKey);

        // Добавление пин-кода и текущего времени
        //String pinCodeAndTime = PIN_CODE + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
//        String pinCodeAndTime = PIN_CODE;
//        verificationSignature.update(pinCodeAndTime.getBytes(StandardCharsets.UTF_8));
        verificationSignature.update(fileContent);

        return verificationSignature.verify(signature);
    }
}