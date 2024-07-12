package su.opencode.digsigcik.synapi.dto;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyDto {
    private boolean valid;
    private String verifyResultMessage;
    private SesUserInfoDto userInfo;
}
