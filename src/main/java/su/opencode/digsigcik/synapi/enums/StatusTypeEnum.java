package su.opencode.digsigcik.synapi.enums;

public enum StatusTypeEnum {
//Не подписана доверителем, Действительна, Просрочена, Отозвана
    NOT_SIGNED("Не подписана доверителем"),
    VALID("Действительна"),
    EXPIRED("Просрочена"),
    WITHDRAWN("Отозвана"),
    UNKNOWN("Неизвестный статус");

    private String title;

    StatusTypeEnum(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
