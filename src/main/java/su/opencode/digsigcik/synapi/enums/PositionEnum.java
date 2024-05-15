package su.opencode.digsigcik.synapi.enums;

public enum PositionEnum {
    DIRECTOR("Директор"),
    WORKER("Работник");

    private final String title;

    PositionEnum(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
