package su.opencode.digsigcik.m4d.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum StatusTypeEnum {
    NOT_SIGNED("Отсутствует ЭП"),
    VALID("Действительна"),
    EXPIRED("Не действительна"),
    WITHDRAWN("Отозвана"),
    UNKNOWN("Неизвестный статус");

    private final String title;
}
