package su.opencode.digsigcik.m4d.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum StatusRequestTypeEnum {
    ONCHECK("Проверка"),
    ERROR("Ошибка"),
    PROCESSING("Обрабатывается"),
    REJECTED("Отклонена"),
    CREATED("Создана"),
    ACTIVE("Действует"),
    EXPIRED("Действие истекло"),
    REVOKED("Действие прекращено");

    private final String title;
}
