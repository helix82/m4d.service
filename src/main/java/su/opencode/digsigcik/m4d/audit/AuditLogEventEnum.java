package su.opencode.digsigcik.m4d.audit;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AuditLogEventEnum {
    NA(AuditLogTopicEnum.NA, "N/A"),

    // Идут с фронта
    SIGN_ERROR(AuditLogTopicEnum.GENERAL_SIGN, "Ошибка с формы"),
    SIGN_CANCEL(AuditLogTopicEnum.GENERAL_SIGN, "Ошибка с формы"),

    USER_LOGIN(AuditLogTopicEnum.AUTH, "Пользователь вошел в СЭП"),
    USER_LOGOUT(AuditLogTopicEnum.AUTH, "Пользователь вышел из СЭП"),

    TEMPLATE_UPLOAD(AuditLogTopicEnum.TEMPLATE, "Загрузка шаблона заявления в СЭП"),
    TEMPLATE_DELETE(AuditLogTopicEnum.TEMPLATE, "Удаление шаблона заявленияиз СЭП"),

    CA_CERT_UPLOAD(AuditLogTopicEnum.CA_CERTS, "Добавление сертификата аккредитованного УЦ"),
    CA_CERT_SAVE(AuditLogTopicEnum.CA_CERTS, "Загрузка сведений о сертификатах УЦ в СЭП"),
    CA_CERT_DELETED(AuditLogTopicEnum.CA_CERTS, "Удаление сертификата аккредитованного УЦ"),

    TRUST_CERTS_UPLOAD(AuditLogTopicEnum.TRUST_CERTS, "Добавление сертификата доверенного УЦ"),
    TRUST_CERTS_DELETE(AuditLogTopicEnum.TRUST_CERTS, "Удаление сертификата доверенного УЦ"),

    USER_CERTS_UPLOAD(AuditLogTopicEnum.USER_CERT, "Добавление сертификата пользователя"),
    USER_CERTS_PARSE(AuditLogTopicEnum.USER_CERT, "Считывание сертификата пользователя"),
    USER_CERTS_DELETE(AuditLogTopicEnum.USER_CERT, "Удаление связи с сертификатом пользователя"),

    SIMPLE_SIGN_CREATE(AuditLogTopicEnum.SIMPLE_SIGN, "Создание простой ЭП"),
    SIMPLE_SIGN_UPDATE(AuditLogTopicEnum.SIMPLE_SIGN, "Обновление простой ЭП"),
    SIMPLE_SIGN_DELETE(AuditLogTopicEnum.SIMPLE_SIGN, "Удаление простой ЭП"),
    SIMPLE_SIGN_SIGNING(AuditLogTopicEnum.SIMPLE_SIGN, "Формирование ЭП с помощью ПЭП"),

    CRL_USING(AuditLogTopicEnum.CRL, "Использование СОС для сверки сертификатов"),
    CRL_UPLOAD(AuditLogTopicEnum.CRL, "Добавление СОС"),
    CRL_DELETE(AuditLogTopicEnum.CRL, "Удаление СОС"),

    DIG_SIGN_CREATE(AuditLogTopicEnum.GENERAL_SIGN, "Формирование ЭП с помощью КЭП или НЭП"),
    DIG_SIGN_VERIFY(AuditLogTopicEnum.GENERAL_SIGN, "Выполнена проверка ЭП"),

    TLS_SERV_PROVIDER_UPLOAD(AuditLogTopicEnum.TLS_SERV_PROVIDER, "Загрузка справочника постовщика TLS-сертификата"),
    TLS_SERV_PROVIDER_UPDATE(AuditLogTopicEnum.TLS_SERV_PROVIDER, "Обновление справочника постовщика TLS-сертификата"),
    TLS_SERV_PROVIDER_DELETE(AuditLogTopicEnum.TLS_SERV_PROVIDER, "Загрузка справочника постовщика TLS-сертификата"),

    TLS_CA_CERTS_UPLOAD(AuditLogTopicEnum.TLS_CA_CERTS, "Загрузка корневого или промежуточного TLS-сертификата"),
    TLS_CA_CERTS_CREATE(AuditLogTopicEnum.TLS_CA_CERTS, "Создание корневого или промежуточного TLS-сертификата"),
    TLS_CA_CERTS_DELETE(AuditLogTopicEnum.TLS_CA_CERTS, "Удаление корневого или промежуточного TLS-сертификата"),

    TLS_SERV_CERTS_UPLOAD(AuditLogTopicEnum.TLS_SERVER_CERTS, "Загрузка серверного сертификата TLS"),
    TLS_SERV_CERTS_DELETE(AuditLogTopicEnum.TLS_SERVER_CERTS, "Удаление серверного сертификата TLS"),
    TLS_SERV_CERTS_REISSUE(AuditLogTopicEnum.TLS_SERVER_CERTS, "Перевыпуск серверного сертификата TLS"),
    TLS_SERV_CERTS_REVOKE(AuditLogTopicEnum.TLS_SERVER_CERTS, "Отзыв серверного сертификата TLS"),

    TLS_CERTS_ORDER_UPLOAD(AuditLogTopicEnum.TLS_CERTS_ORDER, "Отзыв серверного сертификата TLS"),
    TLS_CERTS_ORDER_DELETE(AuditLogTopicEnum.TLS_CERTS_ORDER, "Отзыв серверного сертификата TLS"),
    TLS_CERTS_ORDER_CERT_RELEASE(AuditLogTopicEnum.TLS_CERTS_ORDER, "Выпуск серверного сертификата TLS"),

    M4D_CREATE(AuditLogTopicEnum.M4D, "Добавление МЧД"),
    M4D_UPDATE(AuditLogTopicEnum.M4D, "Обновление МЧД"),
    M4D_DELETE(AuditLogTopicEnum.M4D, "Удаление МЧД"),
    M4D_VERIFY(AuditLogTopicEnum.M4D, "Верификация контейнера МЧД"),

    M4D_RECALL_CREATE(AuditLogTopicEnum.M4D, "Добавление заявления на отзыв МЧД"),
    M4D_RECALL_UPDATE(AuditLogTopicEnum.M4D, "Обновление заявления на отзыв МЧД"),
    M4D_RECALL_DELETE(AuditLogTopicEnum.M4D, "Удаление заявления на отзыв МЧД"),

    M4D_CREDENTIAL_CREATE(AuditLogTopicEnum.M4D_CREDENTIALS, "Создание полномочия"),
    M4D_CREDENTIAL_UPDATE(AuditLogTopicEnum.M4D_CREDENTIALS, "Обновление полномочия"),
    M4D_CREDENTIAL_DELETE(AuditLogTopicEnum.M4D_CREDENTIALS, "Удаление полномочия из справочника"),

    CONTAINER_VERIFY(AuditLogTopicEnum.VERIFY, "Проверка контейнера");

    //    @Enumerated(EnumType.STRING)
    private AuditLogTopicEnum topic;

    private String description;
}
