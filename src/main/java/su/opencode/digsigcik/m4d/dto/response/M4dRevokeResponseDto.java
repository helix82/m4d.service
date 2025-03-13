package su.opencode.digsigcik.m4d.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M4dRevokeResponseDto {
    private Long id;
    private String argument;
    private String signature;
}
