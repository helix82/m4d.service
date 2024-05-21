package su.opencode.digsigcik.synapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.model.RevokedCertificate;
import su.opencode.digsigcik.synapi.repository.RevokedCertificateRepository;

import java.util.List;

@Service
@Primary
public class RevokedCertificateServiceImpl implements RevokedCertificateService {
    private final RevokedCertificateRepository revokedCertificateRepository;

    @Autowired
    public RevokedCertificateServiceImpl(RevokedCertificateRepository revokedCertificateRepository) {
        this.revokedCertificateRepository = revokedCertificateRepository;
    }

    @Override
    public List<RevokedCertificate> findAllRevokedCertificate() {
        return this.revokedCertificateRepository.findAll();
    }

    @Override
    public RevokedCertificate saveRevokedCertificate(RevokedCertificate revokedCertificate) {
        return this.revokedCertificateRepository.save(revokedCertificate);
    }

    @Override
    public RevokedCertificate findRevokedCertificateById(Long id) {
        return this.revokedCertificateRepository.findRevokedCertificateById(id);
    }

    @Override
    public RevokedCertificate updateRevokedCertificate(RevokedCertificate revokedCertificate) {
        return this.revokedCertificateRepository.save(revokedCertificate);
    }

    @Override
    public void deleteRevokedCertificate(Long id) {
        this.revokedCertificateRepository.deleteById(id);
    }
}
