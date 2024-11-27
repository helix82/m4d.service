package su.opencode.digsigcik.m4d.util;

import java.math.BigInteger;

public class Base58 {
    private static final String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
    private static final BigInteger BASE = BigInteger.valueOf(ALPHABET.length());

    public static String encode(byte[] input) {
        BigInteger bi = new BigInteger(1, input);
        StringBuilder result = new StringBuilder();

        while (bi.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] divideAndRemainder = bi.divideAndRemainder(BASE);
            bi = divideAndRemainder[0];
            result.insert(0, ALPHABET.charAt(divideAndRemainder[1].intValue()));
        }

        // Обработка ведущих нулей
        for (byte b : input) {
            if (b == 0) {
                result.insert(0, ALPHABET.charAt(0));
            } else {
                break;
            }
        }

        return result.toString();
    }

    public static byte[] decode(String input) {
        BigInteger bi = BigInteger.ZERO;
        for (char c : input.toCharArray()) {
            bi = bi.multiply(BASE).add(BigInteger.valueOf(ALPHABET.indexOf(c)));
        }

        byte[] bytes = bi.toByteArray();
        // Удаление ведущих нулей
        if (bytes[0] == 0) {
            byte[] tmp = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, tmp, 0, tmp.length);
            return tmp;
        }
        return bytes;
    }
}
