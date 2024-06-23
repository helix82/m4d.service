package su.opencode.digsigcik.synapi.model;

import lombok.Data;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name="ses")
@Transactional
public class Ses {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_login", columnDefinition = "VARCHAR(50)")
    private String userLogin;

    @Column(name = "file_hash", columnDefinition = "VARCHAR(64)")
    private String fileHash;

    @Column(name = "signature", columnDefinition = "BLOB")
    private String signature;

    @Column(name = "operation_date", columnDefinition = "VARCHAR(29)")
    private String operationDate;

    @Column(name = "container_id")
    private Long containerId;
}
