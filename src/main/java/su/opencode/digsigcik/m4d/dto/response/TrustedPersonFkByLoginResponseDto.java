package su.opencode.digsigcik.m4d.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrustedPersonFkByLoginResponseDto {
    private Long id;
    private String login;
    private String userID;
    private String codeTofk;
    private String nameTofk;
    private String organization;
}
