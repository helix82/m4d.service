package su.opencode.digsigcik.synapi.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportContainerDTO {
    private byte[] signCheck;
    private byte[] signReport;

}
