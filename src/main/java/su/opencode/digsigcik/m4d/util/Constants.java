package su.opencode.digsigcik.m4d.util;

/**
 * Descriptions of constants.
 */

public interface Constants {
    String API_V_1_SES_FILES = "/api/v1/sesFiles";
    String GET_CONTAINER = "/getContainer";
    // namespaces
    String XML_ELEMENT_MCHD = "Доверенность";
    String XML_ATTRIBUTE_MCHD_XSI = "xmlns:xsi";
    String XML_ATTRIBUTE_MCHD_XSD = "xmlns:xsd";
    String XML_ATTRIBUTE_MCHD_XMLNS = "xmlns";
    String XML_ATTRIBUTE_MCHD_FORMAT_VERSION = "ВерсФорм";
    String XML_ATTRIBUTE_MCHD_FORMAT_ELEMENTS = "ПрЭлФорм";
    String XML_ATTRIBUTE_MCHD_FILE_ID = "ИдФайл";
    String XML_ELEMENT_DOCUMENT = "Документ";
    String XML_ELEMENT_MCHD_SHORT = "Довер";
    String XML_ELEMENT_MRPA = "СвДов";
    String XML_ATTRIBUTE_MRPA_TYPE = "ВидДовер";
    String XML_ATTRIBUTE_MRPA_RETRUST = "ПрПередов";
    String XML_ATTRIBUTE_MRPA_INTERNAL_NUMBER = "ВнНомДовер";
    String XML_ATTRIBUTE_MRPA_GUID = "НомДовер";
    String XML_ATTRIBUTE_MRPA_DATE_START = "ДатаВыдДовер";
    String XML_ATTRIBUTE_MRPA_DATE_END = "СрокДейст";
    String XML_ATTRIBUTE_MRPA_SYSTEM_INFO = "СведСист";
    String XML_ELEMENT_PRINCIPALS = "СвДоверит";
    String XML_ATTRIBUTE_PRINCIPAL_TYPE = "ТипДоверит";
    String XML_ELEMENT_INFO_PRINCIPAL = "Доверит";
    String XML_ELEMENT_LEGAL_ENTITY = "РосОргДовер";
    String XML_ATTRIBUTE_LEGAL_ENTITY_MANAGEMENT_COMPANY = "ЕИОУК";
    String XML_ATTRIBUTE_LEGAL_ENTITY_INDIVIDUAL = "ЕИОФЛ";
    String XML_ATTRIBUTE_LEGAL_ENTITY_INDIVIDUAL_ENTR = "ЕИОИП";
    String XML_ELEMENT_ORGANISATION = "СвРосОрг";
    String XML_ATTRIBUTE_ORGANISATION_NAME = "НаимОрг";
    String XML_ATTRIBUTE_ORGANISATION_INN = "ИННЮЛ";
    String XML_ATTRIBUTE_ORGANISATION_KPP = "КПП";
    String XML_ATTRIBUTE_ORGANISATION_OGRN = "ОГРН";
    String XML_ELEMENT_ORGANISATION_ADDRESS_REGION = "АдрРег";
    String XML_ELEMENT_ORGANISATION_ADDRESS = "АдрРФ";
    String XML_ATTRIBUTE_ORGANISATION_ADDRESS_REGION = "Регион";
    String XML_ELEMENT_PERSON_PRINCIPAL = "ЛицоБезДов";
    String XML_ATTRIBUTE_PERSON_PRINCIPAL_TYPE = "ПолнЮЛ";
    String XML_ELEMENT_PRINCIPAL = "СвФЛ";
    String XML_ELEMENT_PRINCIPAL_DETAIL = "СведФЛ";
    String XML_ELEMENT_FIO = "ФИО";
    String XML_ATTRIBUTE_FIO_NAME = "Имя";
    String XML_ATTRIBUTE_FIO_SURNAME = "Фамилия";
    String XML_ATTRIBUTE_FIO_PATRONYMIC = "Отчество";
    String XML_ELEMENT_INFO_REPRESENTATIVE = "СвУпПред";
    String XML_ATTRIBUTE_INFO_REPRESENTATIVE_TYPE = "ТипПред";
    String XML_ELEMENT_REPRESENTATIVE = "Пред";
    String XML_ELEMENT_DETAIL_REPRESENTATIVE = "СведФизЛ";
    String XML_ATTRIBUTE_INN = "ИННФЛ";
    String XML_ATTRIBUTE_SNILS = "СНИЛС";
    String XML_ATTRIBUTE_JOB_TITLE = "Должность";
    String XML_ELEMENT_INFO_INDIVIDUAL = "СведФЛ";
    String XML_ATTRIBUTE_INFO_INDIVIDUAL_BIRTHDATE = "ДатаРожд";
    String XML_ELEMENT_REPRESENTATIVE_PASSPORT = "УдЛичнФЛ";
    String XML_ATTRIBUTE_REPRESENTATIVE_PASSPORT_TYPE = "КодВидДок";
    String XML_ATTRIBUTE_REPRESENTATIVE_PASSPORT_SERIES_NUMBER = "СерНомДок";
    String XML_ATTRIBUTE_REPRESENTATIVE_PASSPORT_DATE = "ДатаДок";
    String XML_ATTRIBUTE_REPRESENTATIVE_PASSPORT_WHO = "ВыдДок";
    String XML_ATTRIBUTE_REPRESENTATIVE_PASSPORT_CODE = "КодВыдДок";
    String XML_ELEMENT_CREDENTIAL = "СвПолн";
    String XML_ATTRIBUTE_CREDENTIAL_TYPE = "ТипПолн";
    String XML_ATTRIBUTE_CREDENTIAL_IMPL = "ПрСовмПолн";
    String XML_ATTRIBUTE_CREDENTIAL_LOSS = "ПрУтрПолн";
    String XML_ELEMENT_CREDENTIAL_LIST = "МашПолн";
    String XML_ATTRIBUTE_CREDENTIAL_CODE = "КодПолн";
    String XML_ATTRIBUTE_CREDENTIAL_NAME = "НаимПолн";

    String XML_ELEMENT_RECALL_FILE = "Файл";
    String XML_ELEMENT_RECALL_INFO = "СвЗаяв";
    String XML_ATTRIBUTE_RECALL_GUID = "НомДовер";
    String XML_ATTRIBUTE_RECALL_ARGUMENT = "ПричОтз";
    String XML_ELEMENT_RECALL_WHO = "СвЗаявит";
    String XML_ELEMENT_RECALL_PRINCIPALS = "СвДоверит";
    String XML_ELEMENT_RECALL_LEGAL_ENTITY = "РосОргДовер";
    String XML_ATTRIBUTE_RECALL_ORGANISATION_NAME = "НаимОрг";
    String XML_ATTRIBUTE_RECALL_ORGANISATION_INN = "ИННЮЛ";
    String XML_ATTRIBUTE_RECALL_ORGANISATION_KPP = "КПП";
    String XML_ATTRIBUTE_RECALL_ORGANISATION_OGRN = "ОГРН";
    String XML_ELEMENT_RECALL_REPRESENTATIVE = "СвПред";
    String XML_ELEMENT_RECALL_DETAIL_REPRESENTATIVE = "СведФизЛ";
    String XML_ATTRIBUTE_RECALL_INN = "ИННФЛ";
    String XML_ATTRIBUTE_RECALL_SNILS = "СНИЛС";
    String XML_ELEMENT_RECALL_FIO = "ФИО";
    String XML_ATTRIBUTE_RECALL_FIO_NAME = "Имя";
    String XML_ATTRIBUTE_RECALL_FIO_SURNAME = "Фамилия";
    String XML_ATTRIBUTE_RECALL_FIO_PATRONYMIC = "Отчество";

    String PARAMETER_MRPA_ID_DESCRIPTION = "ИД МЧД";
    String PARAMETER_MRPA_RECALL_ID_DESCRIPTION = "ИД заявления на отзыв МЧД";
    String PARAMETER_CREDENTIAL_ID_DESCRIPTION = "ИД полномочия";
    String PARAMETER_CREDENTIAL_CODE_OR_NAME_DESCRIPTION = "Код или наименование полномочия";

    String CONTROLLER_MRPA_FIND_ALL_SUMMARY = "Получить список МЧД с ФИО представителя и ФИО доверителя";
    String CONTROLLER_MRPA_FIND_ALL_DESCRIPTION = "Возвращает список МЧД с ФИО представителя и ФИО доверителя";
    String CONTROLLER_MRPA_SAVE_SUMMARY = "Сохранить данные по МЧД с привязкой полномочий в БД";
    String CONTROLLER_MRPA_SAVE_DESCRIPTION = "Возвращает созданную МЧД с ФИО представителя и ФИО доверителя";
    String CONTROLLER_MRPA_FIND_BY_ID_SUMMARY = "Получить МЧД по ИД МЧД";
    String CONTROLLER_MRPA_FIND_BY_ID_DESCRIPTION = "Возвращает МЧД";
    String CONTROLLER_MRPA_UPDATE_SUMMARY = "Изменить данные по МЧД";
    String CONTROLLER_MRPA_UPDATE_DESCRIPTION = "Возвращает измененную МЧД с ФИО представителя и ФИО доверителя";
    String CONTROLLER_MRPA_DELETE_SUMMARY = "Удалить данные по МЧД";
    String CONTROLLER_MRPA_DELETE_DESCRIPTION = "Возвращает true при удачном удалении";
    String CONTROLLER_MRPA_UPLOAD_SUMMARY = "Загрузить ЭП";
    String CONTROLLER_MRPA_UPLOAD_DESCRIPTION = "Возвращает МЧД с ФИО представителя и ФИО доверителя";
    String CONTROLLER_MRPA_UPLOAD_XML_SUMMARY = "Импорт МЧД";
    String CONTROLLER_MRPA_UPLOAD_XML_DESCRIPTION = "Возвращает МЧД с ФИО представителя и ФИО доверителя";
    String CONTROLLER_MRPA_CLEAR_SIG_SUMMARY = "Удалить ЭП МЧД";
    String CONTROLLER_MRPA_CLEAR_SIG_DESCRIPTION = "Возвращает МЧД с ФИО представителя и ФИО доверителя";
    String CONTROLLER_MRPA_FIND_ALL_FOR_USER_SUMMARY = "Получить список МЧД пользователя";
    String CONTROLLER_MRPA_FIND_ALL_FOR_USER_DESCRIPTION = "Возвращает список МЧД пользователя";
    String CONTROLLER_MRPA_RECALL_FIND_ALL_SUMMARY = "Получить список заявлений на отзыв МЧД";
    String CONTROLLER_MRPA_RECALL_FIND_ALL_DESCRIPTION = "Возвращает список заявлений на отзыв МЧД";
    String CONTROLLER_MRPA_RECALL_FIND_BY_ID_SUMMARY = "Получить заявление на отзыв МЧД по ИД";
    String CONTROLLER_MRPA_RECALL_FIND_BY_ID_DESCRIPTION = "Возвращает заявление на отзыв МЧД по ИД";
    String CONTROLLER_MRPA_RECALL_SAVE_SUMMARY = "Сохранить заявление на отзыв МЧД";
    String CONTROLLER_MRPA_RECALL_SAVE_DESCRIPTION = "Возвращает заявление на отзыв МЧД";
    String CONTROLLER_MRPA_RECALL_UPDATE_SUMMARY = "Изменить заявление на отзыв МЧД";
    String CONTROLLER_MRPA_RECALL_UPDATE_DESCRIPTION = "Возвращает заявление на отзыв МЧД";
    String CONTROLLER_MRPA_RECALL_DELETE_SUMMARY = "Удалить заявление на отзыв МЧД";
    String CONTROLLER_MRPA_RECALL_DELETE_DESCRIPTION = "Возвращает true при удачном удалении";
    String CONTROLLER_MRPA_RECALL_FIND_ALL_FOR_USER_SUMMARY = "Получить список заявлени на отзыв МЧД пользователя";
    String CONTROLLER_MRPA_RECALL_FIND_ALL_FOR_USER_DESCRIPTION = "Возвращает список заявлений на отзыв МЧД пользователя";
    String CONTROLLER_MRPA_RECALL_UPLOAD_SUMMARY = "Загрузить ЭП заявления на отзыв МЧД";
    String CONTROLLER_MRPA_RECALL_UPLOAD_DESCRIPTION = "Возвращает заявление на отзыв МЧД";

    String CONTROLLER_CREDENTIAL_FIND_ALL_SUMMARY = "Получить список полномочий";
    String CONTROLLER_CREDENTIAL_FIND_ALL_DESCRIPTION = "Возвращает список полномочий";
    String CONTROLLER_CREDENTIAL_FIND_BY_CODE_OR_NAME_SUMMARY = "Получить список полномочий с фильтром по коду или наименованию";
    String CONTROLLER_CREDENTIAL_FIND_BY_CODE_OR_NAME_DESCRIPTION = "Возвращает список полномочий с фильтром по коду или наименованию";
    String CONTROLLER_CREDENTIAL_FIND_BY_MRPA_ID_SUMMARY = "Получить список полномочий с фильтром по ИД МЧД";
    String CONTROLLER_CREDENTIAL_FIND_BY_MRPA_ID_DESCRIPTION = "Возвращает список полномочий";
    String CONTROLLER_CREDENTIAL_SAVE_SUMMARY = "Сохранить запись полномочия";
    String CONTROLLER_CREDENTIAL_SAVE_DESCRIPTION = "Возвращает созданную запись полномочия";
    String CONTROLLER_CREDENTIAL_FIND_BY_ID_SUMMARY = "Получить запись полномочия по ИД";
    String CONTROLLER_CREDENTIAL_FIND_BY_ID_DESCRIPTION = "Возвращает запись полномочия";
    String CONTROLLER_CREDENTIAL_UPDATE_SUMMARY = "Изменить запись полномочия";
    String CONTROLLER_CREDENTIAL_UPDATE_DESCRIPTION = "Возвращает измененную запись полномочия";
    String CONTROLLER_CREDENTIAL_DELETE_SUMMARY = "Удалить запись полномочия по ИД";
    String CONTROLLER_CREDENTIAL_DELETE_DESCRIPTION = "Возвращает true при удачном удалении";

    String CONTROLLER_TEST_KAFKA_SUMMARY = "Отправляет логин пользователя через брокер сообщений в другой микросервис";
    String CONTROLLER_TEST_KAFKA_DESCRIPTION = "Возвращает логин, если отправилось";

    String API_RESPONSE_CODE_200_MESSAGE = "Успешно"; //Successfully
    String API_RESPONSE_CODE_201_MESSAGE = "Создано";
    String API_RESPONSE_CODE_204_MESSAGE = "Нет контента";
    String API_RESPONSE_CODE_400_MESSAGE = "Плохой запрос"; //Not found
    String API_RESPONSE_CODE_404_MESSAGE = "Не найдено"; //Not found

    String NULL = "null";
    String ID_IS_NULL = String.format("ID is %s", NULL);
    String MRPA_IS_NULL = String.format("MRPA is %s", NULL);
    String MRPA_RECALL_IS_NULL = String.format("MRPA recall is %s", NULL);
    String MRPA_RECALL_ERROR_CONVERT = "Ошибка конвертирования ДТО заявления на отзыв МЧД";
    String NULL_OR_EMPTY_ERROR_MESSAGE = String.format("%s or empty!", NULL);
    String MRPA_REGISTRY_IS_NULL = String.format("Mrpa registry is %s!", NULL);
    String MRPA_REGISTRY_IS_EXISTS = "Добавляемая МЧД уже существует в базе";
    String MRPA_REGISTRY_NOT_FULL = "Недостаточно данных для сохранения МЧД";
    String MRPA_REGISTRY_CREDENTIAL_IS_NULL = String.format("Assign Credential to Mrpa registry is %s!", NULL);
    String MRPA_REGISTRY_XML_IS_NULL = "XML МЧД не найден";
    String MRPA_REGISTRY_WRONG_FORMAT = "Не верный формат ЭП";
    String MRPA_REGISTRY_WRONG_FORMAT_XML = "Не верный формат XML";
    String MRPA_REGISTRY_XML_NOT_VERIFY = "XML не прошел проверку";
    String MRPA_REGISTRY_NOT_VERIFY = "Контейнер не прошел проверку";
    String ORGANISATION_IS_NULL = String.format("Organisation is %s!", NULL);
    String PRINCIPAL_IS_NULL = String.format("Principal is %s!", NULL);
    String REPRESENTATIVE_IS_NULL = String.format("Representative is %s!", NULL);

    String NOT_FOUND = "not found!";
}
