package su.opencode.digsigcik.synapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TypeStorageCertificateEnum {
    //LOCAL, CLOUD
    LOCAL("Локально"),
    CLOUD("В облаке"),
    UNKNOWN("Неизвестный тип");

    private final String title;
}
