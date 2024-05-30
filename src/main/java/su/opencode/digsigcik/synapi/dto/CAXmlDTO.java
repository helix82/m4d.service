package su.opencode.digsigcik.synapi.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class CAXmlDTO {
    private String address; //список отозванных сертификатов в виде ссылки на crl-файлы
    private String issuedTo; //кому выдан сертификат, надо вытащить значению по ключю CN, это будет наименование УЦ
    private Timestamp dateStart; //дата начала действия сертификата
    private Timestamp dateEnd; //дата окончания действия сертификата
    private String certificate; //сам сертификат, в виде длинной строки
    private String status; //статус сертификата: действует, приостановлена, прекращена
}
