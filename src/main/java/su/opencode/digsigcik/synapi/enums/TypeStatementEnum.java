package su.opencode.digsigcik.synapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TypeStatementEnum {
    RELEASE("Выпуск"),
    REISSUE("Перевыпуск"),
    RECALL("Отзыв");

    private final String title;
}
