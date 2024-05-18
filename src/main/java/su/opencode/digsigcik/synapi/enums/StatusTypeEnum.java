package su.opencode.digsigcik.synapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum StatusTypeEnum {
//Не подписана доверителем, Действительна, Просрочена, Отозвана
    NOT_SIGNED("Не подписана доверителем"),
    VALID("Действительна"),
    EXPIRED("Просрочена"),
    WITHDRAWN("Отозвана"),
    UNKNOWN("Неизвестный статус");

    private final String title;
}
