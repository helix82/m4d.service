package su.opencode.digsigcik.synapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseErrorDto {
    private String message;
    private Integer code;

    public static ResponseErrorDto createResponseErrorDto(String message, HttpStatus status) {
        return ResponseErrorDto.builder()
                .code(status.value())
                .message(message)
                .build();
    }
}
