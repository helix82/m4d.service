package su.opencode.digsigcik.m4d.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FileLogicalTypeEnum {

    CONTAINER("container"),
    SIGNATURE("signature"),
    CERTIFICATE("certificate"),
    DOCUMENT("document");

    private String value;

    FileLogicalTypeEnum(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static FileLogicalTypeEnum fromValue(String value) {
        for (FileLogicalTypeEnum b : FileLogicalTypeEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}
