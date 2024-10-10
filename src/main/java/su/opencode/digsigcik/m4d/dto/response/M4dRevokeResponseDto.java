package su.opencode.digsigcik.m4d.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import su.opencode.digsigcik.m4d.enums.AuthorTypeEnum;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M4dRevokeResponseDto {
    private Long id;
    private AuthorTypeEnum author;
    private String argument;
    private String signature;
}
