package su.opencode.digsigcik.synapi.enums;

public enum HashAlgorithm {
    GOST3411("GOST3411"),
    GOST3411_2012_256("GOST3411_2012_256"),
    GOST3411_2012_512("GOST3411_2012_512"),
    SHA1("SHA-1"),
    SHA224("SHA-224"),
    SHA256("SHA-256"),
    SHA384("SHA-384"),
    SHA512("SHA-512"),
    SHA512_224("SHA-512/224"),
    SHA512_256("SHA-512/256"),
    SHA3_224("SHA3-224"),
    SHA3_256("SHA3-256"),
    SHA3_384("SHA3-384"),
    SHA3_512("SHA3-512"),
    MD2("MD2"),
    MD5("MD5");

    private final String algorithmName;

    HashAlgorithm(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public static HashAlgorithm fromString(String name) {
        for (HashAlgorithm algorithm : values()) {
            if (algorithm.name().equalsIgnoreCase(name)) {
                return algorithm;
            }
        }
        throw new IllegalArgumentException("Invalid hash algorithm: " + name);
    }
}