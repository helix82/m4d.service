package su.opencode.digsigcik.m4d.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TypeUserCertificateEnum {
    QUALIFIED("Квалифицированный"),
    NOT_QUALIFIED("Неквалифицированный"),
    SIMPLE("Простой"),
    UNKNOWN("Неизвестный тип");

    private final String title;
}
