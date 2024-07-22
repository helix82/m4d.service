package su.opencode.digsigcik.synapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;

@Entity
@Data
@Table(name = "settings")
@AllArgsConstructor
@NoArgsConstructor
public class SettingsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Column(name = "setting_key")
    private String key;
    @NotNull
    @Column(name = "setting_value")
    private String value;

}
