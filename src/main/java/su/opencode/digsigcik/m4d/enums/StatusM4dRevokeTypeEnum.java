package su.opencode.digsigcik.m4d.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum StatusM4dRevokeTypeEnum {
    NEW("Черновик"),
    ACTIVE("Подписано");

    private final String title;
}
