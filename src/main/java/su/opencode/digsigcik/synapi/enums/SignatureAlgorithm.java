package su.opencode.digsigcik.synapi.enums;

public enum SignatureAlgorithm {
    GOST3411withGOST3410EL("GOST3411withGOST3410EL"),
    GOST3411withGOST3410DHEL("GOST3411withGOST3410DHEL"),
    GOST3411_2012_256withGOST3410_2012_256("GOST3411_2012_256withGOST3410_2012_256"),
    GOST3411_2012_512withGOST3410_2012_512("GOST3411_2012_512withGOST3410_2012_512"),
    GOST3411_2012_256withGOST3410DH_2012_256("GOST3411_2012_256withGOST3410DH_2012_256"),
    GOST3411_2012_512withGOST3410DH_2012_512("GOST3411_2012_512withGOST3410DH_2012_512"),
    CryptoProSignature("CryptoProSignature"),
    CryptoProSignature_2012_256("CryptoProSignature_2012_256"),
    CryptoProSignature_2012_512("CryptoProSignature_2012_512"),
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
    SHA512withECDSA("SHA512withECDSA"); // Key size must be at most 571 bits\r\n\tat

    private final String algorithmName;

    SignatureAlgorithm(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public static SignatureAlgorithm fromString(String name) {
        for (SignatureAlgorithm algorithm : values()) {
            if (algorithm.name().equalsIgnoreCase(name)) {
                return algorithm;
            }
        }
        throw new IllegalArgumentException("Invalid signature algorithm: " + name);
    }
}