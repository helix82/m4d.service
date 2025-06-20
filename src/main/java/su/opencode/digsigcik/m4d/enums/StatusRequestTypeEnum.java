package su.opencode.digsigcik.m4d.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum StatusRequestTypeEnum {
    ONCHECK("Идет проверка в РР"),
    ERROR("Ошибка"),
    CREATED("Не наступил срок действия"),
    ACTIVE("Действует"),
    EXPIRED("Действие истекло"),
    REVOKED("Отозвана");

    private final String title;
}
