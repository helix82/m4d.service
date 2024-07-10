package su.opencode.digsigcik.synapi.model;

import lombok.*;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;

@Data
@Builder
@Entity
@Table(name = "containerfile")
@Transactional
@AllArgsConstructor
@NoArgsConstructor
public class ContainerFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", columnDefinition = "VARCHAR(36)")
    private String uuid;

    @Column(name = "file_path")
    private String filePath;

}
