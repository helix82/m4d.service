package su.opencode.digsigcik.synapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.common.aliasing.qual.Unique;
import org.springframework.transaction.annotation.Transactional;
import su.opencode.digsigcik.synapi.model.audit.DateAudit;

import javax.persistence.*;

@Data
@Entity
@Table(name="user_projection")
@Transactional
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProjection extends DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(name = "ИД проекции пользователя", example = "1", required = true)
    @JsonProperty("id")
    private Long id;

    @Unique
    @Column(name = "login", columnDefinition = "VARCHAR(25)")
    @Schema(name = "Логин проекции пользователя", example = "ivanov")
    private String login;
}
