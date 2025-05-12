package su.opencode.digsigcik.m4d.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CryptoProCloudBufferDto {
    private Long certificateId;
    private String userLogin;
    private String pinCode;
    private boolean detached;
}
