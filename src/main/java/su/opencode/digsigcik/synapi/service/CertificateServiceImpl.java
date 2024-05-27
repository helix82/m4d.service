package su.opencode.digsigcik.synapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.model.Certificate;
import su.opencode.digsigcik.synapi.repository.CertificateRepository;

import java.util.List;

@Service
@Primary
public class CertificateServiceImpl implements CertificateService {
    private final CertificateRepository certificateRepository;

    @Autowired
    public CertificateServiceImpl(CertificateRepository certificateRepository) {
        this.certificateRepository = certificateRepository;
    }

    @Override
    public List<Certificate> findAllCertificate() {
        return this.certificateRepository.findAll();
    }

    @Override
    public Certificate saveCertificate(Certificate certificate) {
        return this.certificateRepository.save(certificate);
    }

    @Override
    public Certificate findCertificateById(Long id) {
        return this.certificateRepository.findCertificateById(id);
    }

    @Override
    public Certificate updateCertificate(Certificate certificate) {
        return this.certificateRepository.save(certificate);
    }

    @Override
    public void deleteCertificate(Long id) {
        this.certificateRepository.deleteById(id);
    }
}
