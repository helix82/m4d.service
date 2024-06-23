package example;


import java.io.FileInputStream;
import java.security.*;
import java.util.Base64;
import java.util.Scanner;

public class FileSignature {
    private static final String ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    public static void main(String[] args) {
        try {
            // Генерация ключевой пары
            KeyPair keyPair = generateKeyPair();

            // Получение пин-кода от пользователя
            Scanner scanner = new Scanner(System.in);
            System.out.print("Введите пин-код: ");
            String pinCode = scanner.nextLine();

            // Подписание файла
            String filePath = "file.txt";
            byte[] signature = signFile(filePath, keyPair.getPrivate(), pinCode);
            System.out.println("Подпись файла: " + Base64.getEncoder().encodeToString(signature));

            // Проверка подписи файла
            boolean isValid = verifySignature(filePath, keyPair.getPublic(), signature);
            System.out.println("Подпись файла действительна: " + isValid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }

    private static byte[] signFile(String filePath, PrivateKey privateKey, String pinCode) throws Exception {
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(privateKey, new SecureRandom(pinCode.getBytes()));

        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                signature.update(buffer, 0, read);
            }
        }

        return signature.sign();
    }

    private static boolean verifySignature(String filePath, PublicKey publicKey, byte[] signatureBytes) throws Exception {
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(publicKey);

        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                signature.update(buffer, 0, read);
            }
        }

        return signature.verify(signatureBytes);
    }
}