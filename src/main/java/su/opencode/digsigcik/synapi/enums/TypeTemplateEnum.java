package su.opencode.digsigcik.synapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TypeTemplateEnum {
    RELEASE("Выпуск"),
    REISSUE("Перевыпуск"),
    RECALL("Отзыв"),
    UNKNOWN("Неизвестный тип");

    private final String title;
}
