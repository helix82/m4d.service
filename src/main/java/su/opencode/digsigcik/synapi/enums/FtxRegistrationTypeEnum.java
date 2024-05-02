package su.opencode.digsigcik.synapi.enums;

public enum FtxRegistrationTypeEnum {
    //Не зарегистрирована, Зарегистрирована
    NOT_FTX_REGISTERED("Не зарегистрирована"),
    FTX_REGISTERED("Зарегистрирована"),
    UNKNOWN("Неизвестный статус");

    private String title;

    FtxRegistrationTypeEnum(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
