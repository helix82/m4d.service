package su.opencode.digsigcik.m4d.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class RepresentativeResponse {
    private Long id;
    private String name;
    private String surname;
    private String patronymic;
    private String inn;
    private String snils;
    private String birthDate;
    private List<RepresentativePassportResponse> representativePassports;
}
