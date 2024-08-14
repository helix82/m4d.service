package su.opencode.digsigcik.m4d.dto.response;

import lombok.Data;

@Data
public class RepresentativePassportResponse {
    private Long id;
    private String seriesNumber;
    private String dateIssue;
    private String whoIssue;
    private String departmentCode;
    private Long reprsentativeId;
}
