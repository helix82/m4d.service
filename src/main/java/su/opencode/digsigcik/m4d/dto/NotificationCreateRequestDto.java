package su.opencode.digsigcik.m4d.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import su.opencode.digsigcik.m4d.enums.NotificationTypeEnum;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationCreateRequestDto {
    private String login;
    private JsonNode content;
    private NotificationTypeEnum type;
}
