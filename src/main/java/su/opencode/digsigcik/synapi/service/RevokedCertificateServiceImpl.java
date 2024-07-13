package su.opencode.digsigcik.synapi.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.exception.MchdException;
import su.opencode.digsigcik.synapi.model.RevokedCertificate;
import su.opencode.digsigcik.synapi.repository.RevokedCertificateRepository;

import java.util.List;

import static su.opencode.digsigcik.synapi.util.Constants.NOT_FOUND;

@Service
@Primary
public class RevokedCertificateServiceImpl implements RevokedCertificateService{
    private final RevokedCertificateRepository repository;

    public RevokedCertificateServiceImpl(RevokedCertificateRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<RevokedCertificate> findAllRevokedCertificate() {

        return this.repository.findAll();
    }

    @Override
    public RevokedCertificate saveRevokedCertificate(RevokedCertificate revokedCertificate) {
        return this.repository.save(revokedCertificate);
    }

    @Override
    public RevokedCertificate findByRevokedCertificateId(Long id) {
        return this.repository.findRevokedCertificateById(id);
    }

    @Override
    public RevokedCertificate updateRevokedCertificate(RevokedCertificate revokedCertificate) {
        return this.repository.save(revokedCertificate);
    }

    @Override
    public void deleteRevokedCertificate(Long id) {
        RevokedCertificate revokedCertificate = this.repository.findRevokedCertificateById(id);
        if (revokedCertificate == null) {
            throw new MchdException(String.format("Can`t delete Revoked Certificate because Revoked Certificate %s", NOT_FOUND));
        }
        this.repository.deleteById(id);
    }

    @Override
    public Integer countRevokedCertificateByImprint(String imprint){
        return this.repository.countRevokedCertificateByImprint(imprint);
    }
}
