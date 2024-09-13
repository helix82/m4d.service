package su.opencode.digsigcik.m4d.dto.response;

import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
