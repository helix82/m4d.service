package su.opencode.digsigcik.synapi.util;


import static su.opencode.digsigcik.synapi.util.Constant.NULL;

/**
 * Descriptions of constants.
 */

public interface Constants {
    // namespaces
    String XML_ELEMENT_CA = "УдостоверяющийЦентр";
    String XML_ELEMENT_ACC = "АккредитованныеУдостоверяющиеЦентры";
    String XML_ELEMENT_SHC = "ПрограммноАппаратныеКомплексы";
    String XML_ELEMENT_ACCREDITATION_STATUS = "СтатусАккредитации";
    String XML_ELEMENT_SH_SYSTEM = "ПрограммноАппаратныйКомплекс";
    String XML_ELEMENT_KEYS_AUTH_PERSONS = "КлючиУполномоченныхЛиц";
    String XML_ELEMENT_KEY = "Ключ";
    String XML_ELEMENT_REVIEW_LIST_ADDRESS = "АдресаСписковОтзыва";
    String XML_ELEMENT_CERTIFICATES = "Сертификаты";
    String XML_ELEMENT_ADDRESS = "Адрес";
    String XML_ELEMENT_CERTIFICATE_DATA = "ДанныеСертификата";
    String XML_ELEMENT_STATUS = "Статус";
    String XML_ELEMENT_ISSUED_TO = "КомуВыдан";
    String XML_ELEMENT_DATE_START = "ПериодДействияС";
    String XML_ELEMENT_DATE_END = "ПериодДействияДо";
    String XML_ELEMENT_CERT = "Данные";
    String XML_ELEMENT_IMPRINT = "Отпечаток";
    String XML_ELEMENT_SERIAL_NUMBER = "СерийныйНомер";
    String XML_ELEMENT_KEY_ID = "ИдентификаторКлюча";
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
    String XML_ELEMENT_CREDENTIAL_NAME = "ТекстПолн";

    String SCHEMA_CA_TYPE_INPUT_EXAMPLE = "Вручную";
    String SCHEMA_CA_ACCREDITATION_STATUS_EXAMPLE = "Действует";

    String PARAMETER_MRPA_ID_DESCRIPTION = "ИД МЧД";
    String PARAMETER_CA_ID_DESCRIPTION = "ИД сертификата УЦ";
    String PARAMETER_CREDENTIAL_ID_DESCRIPTION = "ИД полномочия";
    String PARAMETER_CREDENTIAL_CODE_OR_NAME_DESCRIPTION = "Код или наименование полномочия";
    String PARAMETER_ORGANISATION_ID_DESCRIPTION = "ИД организации";
    String PARAMETER_PRINCIPAL_ID_DESCRIPTION = "ИД доверителя";
    String PARAMETER_PRINCIPAL_INN_DESCRIPTION = "ИНН доверителя";
    String PARAMETER_REPRESENTATIVE_ID_DESCRIPTION = "ИД представителя";
    String PARAMETER_REPRESENTATIVE_PASSPORT_ID_DESCRIPTION = "ИД паспорта представителя";
    String PARAMETER_REVOKED_CERTIFICATE_ID_DESCRIPTION = "ИД отозванного сертификата";
    String PARAMETER_TRUSTED_CERTIFICATE_ID_DESCRIPTION = "ИД доверенного сертификата";

    String CONTROLLER_MRPA_FIND_ALL_SUMMARY = "Получить список МЧД с ФИО представителя и ФИО доверителя";
    String CONTROLLER_MRPA_FIND_ALL_DESCRIPTION = "Возвращает список МЧД с ФИО представителя и ФИО доверителя";
    String CONTROLLER_MRPA_SAVE_SUMMARY = "Сохранить данные по МЧД с привязкой полномочий в БД";
    String CONTROLLER_MRPA_SAVE_DESCRIPTION = "Возвращает созданную МЧД с ФИО представителя и ФИО доверителя";
    String CONTROLLER_MRPA_FIND_BY_ID_SUMMARY = "Получить МЧД с ФИО представителя и ФИО доверителя по ИД МЧД";
    String CONTROLLER_MRPA_FIND_BY_ID_DESCRIPTION = "Возвращает МЧД с ФИО представителя и ФИО доверителя";
    String CONTROLLER_MRPA_UPDATE_SUMMARY = "Изменить данные по МЧД";
    String CONTROLLER_MRPA_UPDATE_DESCRIPTION = "Возвращает измененную МЧД с ФИО представителя и ФИО доверителя";
    String CONTROLLER_MRPA_DELETE_SUMMARY = "Удалить данные по МЧД";
    String CONTROLLER_MRPA_DELETE_DESCRIPTION = "Возвращает true при удачном удалении";
    String CONTROLLER_MRPA_FIND_BY_PRINCIPAL_INN_SUMMARY = "Получить МЧД с ФИО представителя и ФИО доверителя по ИНН доверителя";
    String CONTROLLER_MRPA_FIND_BY_PRINCIPAL_INN_DESCRIPTION = "Возвращает МЧД с ФИО представителя и ФИО доверителя";
    String CONTROLLER_MRPA_UPLOAD_SUMMARY = "Загрузить ЭП";
    String CONTROLLER_MRPA_UPLOAD_DESCRIPTION = "Возвращает МЧД с ФИО представителя и ФИО доверителя";
    String CONTROLLER_MRPA_RECALL_SUMMARY = "Отозвать МЧД";
    String CONTROLLER_MRPA_RECALL_DESCRIPTION = "Возвращает МЧД с ФИО представителя и ФИО доверителя";
    String CONTROLLER_MRPA_DOWNLOAD_SUMMARY = "Выгрузить МЧД в XML-файл";
    String CONTROLLER_MRPA_DOWNLOAD_DESCRIPTION = "Возвращает XML-структуру";

    String CONTROLLER_CA_TYPE_INPUT_AUTO = "Автоматически";
    String CONTROLLER_CA_TYPE_INPUT_MANUALLY = "Вручную";

    String CONTROLLER_CA_UPLOAD_SUMMARY = "Загрузить аккредитованные УЦ из XML-файла";
    String CONTROLLER_CA_UPLOAD_DESCRIPTION = "Возвращает количество загруженных сертификатов";
    String CONTROLLER_CA_FIND_ALL_SUMMARY = "Получить список аккредитованных УЦ";
    String CONTROLLER_CA_FIND_ALL_DESCRIPTION = "Возвращает список аккредитованных УЦ";
    String CONTROLLER_CA_SAVE_SUMMARY = "Сохранить новую запись аккредитованных УЦ";
    String CONTROLLER_CA_SAVE_DESCRIPTION = "Возвращает сохраненную запись аккредитованной УЦ";
    String CONTROLLER_CA_FIND_BY_ID_SUMMARY = "Получить запись аккредитованной УЦ по ИД";
    String CONTROLLER_CA_FIND_BY_ID_DESCRIPTION = "Возвращает запись аккредитованной УЦ";
    String CONTROLLER_CA_UPDATE_SUMMARY = "Сохранить изменения аккредитованной УЦ";
    String CONTROLLER_CA_UPDATE_DESCRIPTION = "Возвращает запись аккредитованной УЦ";
    String CONTROLLER_CA_DELETE_SUMMARY = "Удалить запись аккредитованной УЦ по ИД";
    String CONTROLLER_CA_DELETE_DESCRIPTION = "Возвращает true при удачном удалении";

    String CONTROLLER_CREDENTIAL_FIND_ALL_SUMMARY = "Получить список полномочий";
    String CONTROLLER_CREDENTIAL_FIND_ALL_DESCRIPTION = "Возвращает список полномочий";
    String CONTROLLER_CREDENTIAL_FIND_BY_CODE_OR_NAME_SUMMARY = "Получить список полномочий с фильтром по коду или наименованию";
    String CONTROLLER_CREDENTIAL_FIND_BY_CODE_OR_NAME_DESCRIPTION = "Возвращает список полномочий с фильтром по коду или наименованию";

    String CONTROLLER_CREDENTIAL_SAVE_SUMMARY = "Сохранить запись полномочия";
    String CONTROLLER_CREDENTIAL_SAVE_DESCRIPTION = "Возвращает созданную запись полномочия";
    String CONTROLLER_CREDENTIAL_FIND_BY_ID_SUMMARY = "Получить запись полномочия по ИД";
    String CONTROLLER_CREDENTIAL_FIND_BY_ID_DESCRIPTION = "Возвращает запись полномочия";
    String CONTROLLER_CREDENTIAL_UPDATE_SUMMARY = "Изменить запись полномочия";
    String CONTROLLER_CREDENTIAL_UPDATE_DESCRIPTION = "Возвращает измененную запись полномочия";
    String CONTROLLER_CREDENTIAL_DELETE_SUMMARY = "Удалить запись полномочия по ИД";
    String CONTROLLER_CREDENTIAL_DELETE_DESCRIPTION = "Возвращает true при удачном удалении";

    String CONTROLLER_ORGANISATION_FIND_ALL_SUMMARY = "Получить список организаций";
    String CONTROLLER_ORGANISATION_FIND_ALL_DESCRIPTION = "Возвращает список организаций";
    String CONTROLLER_ORGANISATION_SAVE_SUMMARY = "Сохранить организацию";
    String CONTROLLER_ORGANISATION_SAVE_DESCRIPTION = "Если указанной организации нет в базе, создаем её и возвращаем, иначе возвращаем уже существующую организацию";
    String CONTROLLER_ORGANISATION_FIND_BY_ID_SUMMARY = "Получить организацию по ИД";
    String CONTROLLER_ORGANISATION_FIND_BY_ID_DESCRIPTION = "Возвращает запись организации";
    String CONTROLLER_ORGANISATION_UPDATE_SUMMARY = "Изменить запись организации";
    String CONTROLLER_ORGANISATION_UPDATE_DESCRIPTION = "Возвращает измененную запись организации";
    String CONTROLLER_ORGANISATION_DELETE_SUMMARY = "Удалить запись организации по ИД";
    String CONTROLLER_ORGANISATION_DELETE_DESCRIPTION = "Возвращает true при удачном удалении";

    String CONTROLLER_PRINCIPAL_FIND_ALL_SUMMARY = "Получить список доверителей";
    String CONTROLLER_PRINCIPAL_FIND_ALL_DESCRIPTION = "Возвращает список доверителей";
    String CONTROLLER_PRINCIPAL_SAVE_SUMMARY = "Сохранить доверителя";
    String CONTROLLER_PRINCIPAL_SAVE_DESCRIPTION = "Возвращает созданного доверителя";
    String CONTROLLER_PRINCIPAL_FIND_BY_ID_SUMMARY = "Получить доверителя по ИД";
    String CONTROLLER_PRINCIPAL_FIND_BY_ID_DESCRIPTION = "Возвращает доверителя";
    String CONTROLLER_PRINCIPAL_UPDATE_SUMMARY = "Изменить запись доверителя";
    String CONTROLLER_PRINCIPAL_UPDATE_DESCRIPTION = "Возвращает измененную запись доверителя";
    String CONTROLLER_PRINCIPAL_DELETE_SUMMARY = "Удалить запись доверителя по ИД";
    String CONTROLLER_PRINCIPAL_DELETE_DESCRIPTION = "Возвращает true при удачном удалении";

    String CONTROLLER_REPRESENTATIVE_FIND_ALL_SUMMARY = "Получить список представителей";
    String CONTROLLER_REPRESENTATIVE_FIND_ALL_DESCRIPTION = "Возвращает список представителей";
    String CONTROLLER_REPRESENTATIVE_SAVE_SUMMARY = "Сохранить представителя";
    String CONTROLLER_REPRESENTATIVE_SAVE_DESCRIPTION = "Возвращает созданного представителя";
    String CONTROLLER_REPRESENTATIVE_FIND_BY_ID_SUMMARY = "Получить представителя по ИД";
    String CONTROLLER_REPRESENTATIVE_FIND_BY_ID_DESCRIPTION = "Возвращает представителя по ИД";
    String CONTROLLER_REPRESENTATIVE_UPDATE_SUMMARY = "Изменить запись представителя";
    String CONTROLLER_REPRESENTATIVE_UPDATE_DESCRIPTION = "Возвращает измененную запись представителя";
    String CONTROLLER_REPRESENTATIVE_DELETE_SUMMARY = "Удалить запись представителя по ИД";
    String CONTROLLER_REPRESENTATIVE_DELETE_DESCRIPTION = "Возвращает true при удачном удалении";

    String CONTROLLER_REPRESENTATIVE_PASSPORT_FIND_ALL_SUMMARY = "Получить список паспортов представителей";
    String CONTROLLER_REPRESENTATIVE_PASSPORT_FIND_ALL_DESCRIPTION = "Возвращает список паспортов представителей";
    String CONTROLLER_REPRESENTATIVE_PASSPORT_SAVE_SUMMARY = "Сохранить паспорт представителя";
    String CONTROLLER_REPRESENTATIVE_PASSPORT_SAVE_DESCRIPTION = "Возвращает созданный паспорт представителя";
    String CONTROLLER_REPRESENTATIVE_PASSPORT_FIND_BY_ID_SUMMARY = "Получить паспорт представителя по ИД";
    String CONTROLLER_REPRESENTATIVE_PASSPORT_FIND_BY_ID_DESCRIPTION = "Возвращает паспорт представителя по ИД";
    String CONTROLLER_REPRESENTATIVE_PASSPORT_UPDATE_SUMMARY = "Изменить запись паспорта представителя";
    String CONTROLLER_REPRESENTATIVE_PASSPORT_UPDATE_DESCRIPTION = "Возвращает измененную запись паспорта представителя";
    String CONTROLLER_REPRESENTATIVE_PASSPORT_DELETE_SUMMARY = "Удалить запись паспорта представителя по ИД";
    String CONTROLLER_REPRESENTATIVE_PASSPORT_DELETE_DESCRIPTION = "Возвращает true при удачном удалении";

    String CONTROLLER_REVOKED_CERTIFICATE_FIND_ALL_SUMMARY = "Получить список отозванных сертификатов";
    String CONTROLLER_REVOKED_CERTIFICATE_FIND_ALL_DESCRIPTION = "Возвращает список отозванных сертификатов";
    String CONTROLLER_REVOKED_CERTIFICATE_SAVE_SUMMARY = "Сохранить отозванный сертификат";
    String CONTROLLER_REVOKED_CERTIFICATE_SAVE_DESCRIPTION = "Возвращает созданный отозванный сертификат";
    String CONTROLLER_REVOKED_CERTIFICATE_FIND_BY_ID_SUMMARY = "Получить отозванный сертификат по ИД";
    String CONTROLLER_REVOKED_CERTIFICATE_FIND_BY_ID_DESCRIPTION = "Возвращает отозванный сертификат по ИД";
    String CONTROLLER_REVOKED_CERTIFICATE_UPDATE_SUMMARY = "Изменить отозванный сертификат";
    String CONTROLLER_REVOKED_CERTIFICATE_UPDATE_DESCRIPTION = "Возвращает измененный отозванный сертификат";
    String CONTROLLER_REVOKED_CERTIFICATE_DELETE_SUMMARY = "Удалить отозванный сертификат по ИД";
    String CONTROLLER_REVOKED_CERTIFICATE_DELETE_DESCRIPTION = "Возвращает true при удачном удалении";

    String CONTROLLER_TRUSTED_CERTIFICATE_FIND_ALL_SUMMARY = "Получить список доверенных сертификатов";
    String CONTROLLER_TRUSTED_CERTIFICATE_FIND_ALL_DESCRIPTION = "Возвращает список доверенных сертификатов";
    String CONTROLLER_TRUSTED_CERTIFICATE_SAVE_SUMMARY = "Сохранить доверенный сертификат";
    String CONTROLLER_TRUSTED_CERTIFICATE_SAVE_DESCRIPTION = "Возвращает созданный доверенный сертификат";
    String CONTROLLER_TRUSTED_CERTIFICATE_FIND_BY_ID_SUMMARY = "Получить доверенный сертификат по ИД";
    String CONTROLLER_TRUSTED_CERTIFICATE_FIND_BY_ID_DESCRIPTION = "Возвращает доверенный сертификат по ИД";
    String CONTROLLER_TRUSTED_CERTIFICATE_UPDATE_SUMMARY = "Изменить доверенный сертификат";
    String CONTROLLER_TRUSTED_CERTIFICATE_UPDATE_DESCRIPTION = "Возвращает доверенный сертификат";
    String CONTROLLER_TRUSTED_CERTIFICATE_DELETE_SUMMARY = "Удалить доверенный сертификат по ИД";
    String CONTROLLER_TRUSTED_CERTIFICATE_DELETE_DESCRIPTION = "Возвращает true при удачном удалении";
    String CONTROLLER_TRUSTED_CERTIFICATE_UPLOAD_SUMMARY = "Загрузить доверенные сертификаты";
    String CONTROLLER_TRUSTED_CERTIFICATE_UPLOAD_DESCRIPTION = "Возвращает количество загруженных сертификатов";

    String API_RESPONSE_CODE_200_MESSAGE = "Успешно"; //Successfully
    String API_RESPONSE_CODE_400_MESSAGE = "Плохой запрос"; //Not found
    String API_RESPONSE_CODE_404_MESSAGE = "Не найдено"; //Not found

    String MRPA_REGISTRY_IS_NULL = String.format("Mrpa registry is %s!", NULL);
    String MRPA_REGISTRY_CREDENTIAL_IS_NULL = String.format("Assign Credential to Mrpa registry is %s!", NULL);
    String CREDENTIAL_IS_NULL = String.format("Credential is %s!", NULL);
    String ORGANISATION_IS_NULL = String.format("Organisation is %s!", NULL);
    String PRINCIPAL_IS_NULL = String.format("Principal is %s!", NULL);
    String REPRESENTATIVE_IS_NULL = String.format("Representative is %s!", NULL);
    String REPRESENTATIVE_PASSPORT_IS_NULL = String.format("Representative passport is %s!", NULL);
    String REVOKED_CERTIFICATE_IS_NULL = String.format("Revoked certificate is %s!", NULL);
    String TRUSTED_CERTIFICATE_IS_NULL = String.format("Trusted certificate is %s!", NULL);
    String TRUSTED_CERTIFICATE_UPLOAD_EXISTS = "Загружаемый доверенный сертификат уже существует в базе!";
    String CA_IS_NULL = String.format("CA is %s!", NULL);



    String CONTROLLER_PIN_CODE_GET_ALL = "Возвращает хэши пинкодов всех пользователей";
    String CONTROLLER_PIN_CODE_CREATE = "Создает пинкод для пользователя";
    String CONTROLLER_PIN_CODE_UPDATE = "Обновляет пинкод для пользователя";
    String CONTROLLER_PIN_CODE_DELETE = "Удаляет пинкод для пользователя";

}
