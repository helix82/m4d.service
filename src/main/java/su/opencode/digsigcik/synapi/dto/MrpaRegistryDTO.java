package su.opencode.digsigcik.synapi.dto;

import lombok.Data;
import org.joda.time.DateTime;

@Data
public class MrpaRegistryDTO {
    private String guid;
    private String internalNumber;
    private String organization;
    private String principal;
    private String representative;
    private DateTime startDate;
    private DateTime endDate;
    private String status;
    private String fixRegistration;
    private String sigPath;
    private String mrpaXmlPath;
    private String recalledXmlPath;
    private String credentials;
}
