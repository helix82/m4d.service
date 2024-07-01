package su.opencode.digsigcik.synapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum StatusStatementEnum {
    IN_PROGRESS("В работе"),
    PROCESSED("Обработано"),
    UNKNOWN("Неизвестный статус");

    private final String title;
}
