package su.opencode.digsigcik.m4d.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M4dCountAndRevokeCountResponseDto {
    private long M4D;
    private long M4DRevoke;
}
