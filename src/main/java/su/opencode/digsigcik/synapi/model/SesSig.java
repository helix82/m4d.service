package su.opencode.digsigcik.synapi.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name="ses_sig")
@Transactional
public class SesSig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "uuid")
    private UUID uuid;
    @Lob
    @Column(name = "ses_xml_sig_file")
    private String sesXmlSigFile;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "ses_xml_sig_id")
    private Set<Ses> ses = new HashSet<>();
}
