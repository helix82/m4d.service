package su.opencode.digsigcik.synapi.enums;

public enum RoleEnum {
    USER("USER"),
    ADMIN("ADMIN");

    private final String title;

    RoleEnum(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
