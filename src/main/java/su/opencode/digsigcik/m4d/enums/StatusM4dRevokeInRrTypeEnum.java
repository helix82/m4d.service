package su.opencode.digsigcik.m4d.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum StatusM4dRevokeInRrTypeEnum {
    ONCHECK("Отправлена на регистрацию в РР"),
    ERROR("Ошибка"),
    SUCCESS("Успех");

    private final String title;
}
