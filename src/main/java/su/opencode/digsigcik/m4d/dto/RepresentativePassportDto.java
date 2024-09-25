package su.opencode.digsigcik.m4d.dto;

import lombok.Data;

@Data
public class RepresentativePassportDto {
    private Long id;
    private String seriesNumber;
    private String dateIssue;
    private String whoIssue;
    private String departmentCode;
    private Long representativeId;
}
