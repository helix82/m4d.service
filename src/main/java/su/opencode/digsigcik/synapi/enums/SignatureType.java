package su.opencode.digsigcik.synapi.enums;

import lombok.Getter;

@Getter
public enum SignatureType {
    CAdES_BES("CAdES_BES"),
    CAdES_T("CAdES_T"),
    CAdES_X_Long_Type_1("CAdES_X_Long_Type_1"),
    CAdES_A("CAdES_A"),
    XAdES_BES("XAdES_BES");
    private final String signatureTypeName;

    SignatureType(String signatureTypeName) {
        this.signatureTypeName = signatureTypeName;
    }

    public static SignatureType fromString(String name) {
        for (SignatureType signatureType : values()) {
            if (signatureType.name().equalsIgnoreCase(name)) {
                return signatureType;
            }
        }
        throw new IllegalArgumentException("Invalid signature type: " + name);
    }
    public static SignatureType fromAlgorithmName(String name) {
        for (SignatureType signatureType : values()) {
            if (signatureType.signatureTypeName.equalsIgnoreCase(name)) {
                return signatureType;
            }
        }
        throw new IllegalArgumentException("Invalid signature type: " + name);
    }

    @Override
    public String toString() {
        return "SignatureType{" +
                "signatureType='" + signatureTypeName + '\'' +
                '}';
    }
}