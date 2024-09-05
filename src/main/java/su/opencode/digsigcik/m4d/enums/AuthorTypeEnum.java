package su.opencode.digsigcik.m4d.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AuthorTypeEnum {
    PRINCIPAL("Доверитель"),
    REPRESENTATIVE("Представитель"),
    UNKNOWN("Неизвестный инициатор");

    private final String title;
}
