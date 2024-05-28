package su.opencode.digsigcik.synapi.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name="ses")
@Transactional
public class Ses {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "operation_id")
    private Long operationId;
    @Column(name = "user_login", columnDefinition = "VARCHAR(50)")
    private String userLogin;
    @Column(name = "signature", columnDefinition = "VARCHAR(100)")
    private String signature;
    @Column(name = "ses_xml_sig_id")
    private Long sesXmlSig;
}
