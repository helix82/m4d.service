package su.opencode.digsigcik.synapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TypeUserCertificateEnum {
    QUALIFIED("Квалифицированный"),
    NOT_QUALIFIED("Неквалифицированный"),
    UNKNOWN("Неизвестный тип");

    private final String title;
}
