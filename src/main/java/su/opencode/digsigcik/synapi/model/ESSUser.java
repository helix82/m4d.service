package su.opencode.digsigcik.synapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.transaction.annotation.Transactional;
import su.opencode.digsigcik.synapi.enums.PositionEnum;
import su.opencode.digsigcik.synapi.enums.RoleEnum;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

//TODO: @vitalik.sahnov Временное решение. Удалить поля, которые относятся к авторизации, после интеграции авторизации
@Data
@Entity
@Table(name="ess_user")
@Transactional
// Electronic signature service user
public class ESSUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(name = "ИД пользователя", example = "1", required = true)
    @JsonProperty("id")
    private Long id;

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

    @Column(name = "pin_code", columnDefinition = "VARCHAR(10)")
    @Schema(name = "Пин-кода", example = "89265390")
    private String pinCode;

    @Column(name = "pin_code_hash", columnDefinition = "VARCHAR(64)")
    @Schema(name = "Хэш пин-кода", example = "cd70b...24f45a")
    private String pinCodeHash;

    @Column(name = "file_hash", columnDefinition = "VARCHAR(64)")
    @Schema(name = "Хэш файла", example = "tasf0b...24145a")
    private String fileHash;

    @Column(name = "signature", columnDefinition = "VARCHAR(159)")
    @Schema(name = "Сигнатура", example = "хэш_пинкода.хэш_файла.дата")
    private String signature;

    @Column(name = "operation_date", columnDefinition = "VARCHAR(29)")
    @Schema(name = "Дата операции", example = "2024-06-18T17:08:59.309763400")
    private String operationDate;

    @Column(name = "password", columnDefinition = "VARCHAR(20)")
    @Schema(name = "Пароль", example = "12345678")
    private String password;

    @Column(name = "email", columnDefinition = "VARCHAR(50)")
    @Schema(name = "Email", example = "ivanov@gmail.com")
    private String email;
}
