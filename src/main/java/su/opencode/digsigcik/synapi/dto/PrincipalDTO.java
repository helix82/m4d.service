package su.opencode.digsigcik.synapi.dto;

import lombok.Data;
import org.joda.time.DateTime;

@Data
public class PrincipalDTO {
    private String name;
    private Integer inn;
    private Integer kpp;
    private Integer snils;
    private DateTime birthDate;
}
