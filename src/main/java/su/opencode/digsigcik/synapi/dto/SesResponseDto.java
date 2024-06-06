package su.opencode.digsigcik.synapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SesResponseDto {
//    private ErrorDto error;
//    private HttpStatus status;
    private String containerPath;
    private SesJsonDto responseSesJson;
}
