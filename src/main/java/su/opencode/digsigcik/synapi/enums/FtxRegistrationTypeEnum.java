package su.opencode.digsigcik.synapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FtxRegistrationTypeEnum {
    //Не зарегистрирована, Зарегистрирована
    NOT_FTX_REGISTERED("Не зарегистрирована"),
    FTX_REGISTERED("Зарегистрирована"),
    UNKNOWN("Неизвестный статус");

    private final String title;
}
