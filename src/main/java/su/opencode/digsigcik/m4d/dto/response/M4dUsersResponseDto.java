package su.opencode.digsigcik.m4d.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class M4dUsersResponseDto {
    private Long id;
    private String guid;
    private String internalNumber;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createDate;
    private Long principalId;
    private String principalInn;
    private String principalSnils;
    private String principalJobTitle;
    private String principalName;
    private String principalSurname;
    private String principalPatronymic;
    private Long representativeId;
    private String representativeInn;
    private String representativeSnils;
    private LocalDateTime representativeBirthDate;
    private String representativeName;
    private String representativeSurname;
    private String representativePatronymic;

}

