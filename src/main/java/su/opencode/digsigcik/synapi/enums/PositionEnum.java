package su.opencode.digsigcik.synapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PositionEnum {
    TLS_ADMIN("Администратор TLS"),
    IOK_OBJECTS_ADMIN("Администратор объектами ИОК"),
    SEP_ADMIN("Администратор СЭП"),
    SEP_USER("Пользователь СЭП");

    private final String title;
}
