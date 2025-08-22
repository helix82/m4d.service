package su.opencode.digsigcik.m4d.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDto {
    private Long id;
    private String login;
    private JsonNode content;
    private String type;
    private Boolean isRead;
    private Instant createdAt;
    private Instant updatedAt;
}
