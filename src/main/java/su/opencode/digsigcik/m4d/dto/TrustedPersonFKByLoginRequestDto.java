package su.opencode.digsigcik.m4d.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrustedPersonFKByLoginRequestDto {
    private String login;
}
