package su.opencode.digsigcik.synapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtPayloadDto {
    @JsonSetter("resource_access")
    private PdDto resourceAccess;
    @JsonSetter("preferred_username")
    private String preferredUsername;
    @JsonSetter("firstname")
    private String firstName;
    @JsonSetter("lastname")
    private String lastName;
    private String patronymic;
    @JsonIgnore
    private Date exp;
    @JsonIgnore
    private Date iat;
}
