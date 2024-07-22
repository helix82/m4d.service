package su.opencode.digsigcik.synapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.checkerframework.common.aliasing.qual.Unique;
import su.opencode.digsigcik.synapi.enums.PositionEnum;
import su.opencode.digsigcik.synapi.enums.RoleEnum;

import javax.persistence.*;

//TODO: @vitalik.sahnov Временное решение. Удалить поля, которые относятся к авторизации, после интеграции авторизации
@Data
@Entity
@Table(name = "ess_user")
// Electronic signature service user
public class ESSUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(name = "ИД пользователя", example = "1", required = true)
    @JsonProperty("id")
    private Long id;

    @Unique
    @Column(name = "login", columnDefinition = "VARCHAR(25)")
    @Schema(name = "Логин пользователя", example = "ivanov")
    private String login;

    @Column(name = "firstname", columnDefinition = "VARCHAR(255)")
    @Schema(name = "Имя")
    private String firstname;

    @Column(name = "lastname", columnDefinition = "VARCHAR(255)")
    @Schema(name = "Фамилия")
    private String lastname;

    @Column(name = "patronymic", columnDefinition = "VARCHAR(255)")
    @Schema(name = "Отчество")
    private String patronymic;

    @Column(name = "inn", columnDefinition = "VARCHAR(30)")
    @Schema(name = "ИНН", example = "832061154574")
    private String inn;

    @Enumerated(EnumType.STRING)
    @Column(name = "position", columnDefinition = "VARCHAR(50)")
    @Schema(name = "Должность", example = "SEP_ADMIN")
    private PositionEnum position;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", columnDefinition = "VARCHAR(50)")
    @Schema(name = "Роль", example = "USER")
    private RoleEnum role;

    @Column(name = "password", columnDefinition = "VARCHAR(20)")
    @Schema(name = "Пароль", example = "12345678")
    private String password;


}
