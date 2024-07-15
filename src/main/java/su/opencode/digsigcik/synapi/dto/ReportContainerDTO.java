package su.opencode.digsigcik.synapi.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReportContainerDTO {
    private byte[] signCheck;
    private byte[] signReport;

}
