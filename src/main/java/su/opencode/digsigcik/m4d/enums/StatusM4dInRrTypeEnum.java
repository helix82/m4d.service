package su.opencode.digsigcik.m4d.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum StatusM4dInRrTypeEnum {
    ONCHECK("Отправлена на регистрацию в РР"),
    ERROR("Ошибка регистрации в РР"),
    REGISTERED("Зарегистрирована в РР"),
    REVOKED("Отозвана в РР");

    private final String title;
}
