package su.opencode.digsigcik.synapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AccreditationStatusEnum {
    VALID("Действует"),
    DISCONTINUED("Прекращена"),
    SUSPENDED("Приостановлена"),
    CANCELED("Аннулирована"),
    UNKNOWN("Неизвестный статус");

    private final String title;
}
