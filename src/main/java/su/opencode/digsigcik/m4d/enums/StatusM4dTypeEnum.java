package su.opencode.digsigcik.m4d.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum StatusM4dTypeEnum {
    NEW("Черновик"),
    ACTIVE("Действует"),
    NOT_ACTIVE("Срок действия не наступил или закончился"),
    REVOKED("Отозвана");

    private final String title;
}
