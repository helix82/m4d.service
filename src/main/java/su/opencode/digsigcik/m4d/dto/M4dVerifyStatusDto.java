package su.opencode.digsigcik.m4d.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class M4dVerifyStatusDto {
    private Long id;
    private String guid;
    private LocalDateTime verifyDate;
    private String status;
    private String source;
    private M4dRegistryResponseDto m4d;
}
