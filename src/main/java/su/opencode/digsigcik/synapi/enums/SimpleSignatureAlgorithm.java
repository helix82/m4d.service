package su.opencode.digsigcik.synapi.enums;

import lombok.Getter;

@Getter
public enum SimpleSignatureAlgorithm {
    NONEwithRSA("NONEwithRSA"),
    MD2withRSA("MD2withRSA"),
    MD5withRSA("MD5withRSA"),
    SHA1withRSA("SHA1withRSA"),
    SHA256withRSA("SHA256withRSA"),
    SHA384withRSA("SHA384withRSA"),
    SHA512withRSA("SHA512withRSA"),
    SHA1withDSA("SHA1withDSA"), //"Internal Server Error",  "trace": "java.security.InvalidParameterException: Key size must be at most 571 bits\r\n\tat
    SHA256withDSA("SHA256withDSA"),
    SHA384withDSA("SHA384withDSA"),
    SHA512withDSA("SHA512withDSA"),
    SHA1withECDSA("SHA1withECDSA"),
    SHA256withECDSA("SHA256withECDSA"), //Key size must be at most 571 bits\r\n\tat
    SHA384withECDSA("SHA384withECDSA"), //Key size must be at most 571 bits\r\n\tat
    SHA512withECDSA("SHA512withECDSA");
    private final String algorithmName;

    SimpleSignatureAlgorithm(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public static SimpleSignatureAlgorithm fromString(String name) {
        for (SimpleSignatureAlgorithm algorithm : values()) {
            if (algorithm.name().equalsIgnoreCase(name)) {
                return algorithm;
            }
        }
        throw new IllegalArgumentException("Invalid signature algorithm: " + name);
    }
    public static SimpleSignatureAlgorithm fromAlgorithmName(String name) {
        for (SimpleSignatureAlgorithm algorithm : values()) {
            if (algorithm.algorithmName.equalsIgnoreCase(name)) {
                return algorithm;
            }
        }
        throw new IllegalArgumentException("Invalid signature algorithm: " + name);
    }

    @Override
    public String toString() {
        return "SignatureAlgorithm{" +
                "algorithmName='" + algorithmName + '\'' +
                '}';
    }
}