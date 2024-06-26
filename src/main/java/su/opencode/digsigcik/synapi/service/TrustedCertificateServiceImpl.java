package su.opencode.digsigcik.synapi.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.exception.MchdException;
import su.opencode.digsigcik.synapi.model.TrustedCertificate;
import su.opencode.digsigcik.synapi.repository.TrustedCertificateRepository;

import java.util.List;

import static su.opencode.digsigcik.synapi.util.Constant.NOT_FOUND;

@Service
@Primary
public class TrustedCertificateServiceImpl implements TrustedCertificateService{
    private final TrustedCertificateRepository repository;

    public TrustedCertificateServiceImpl(TrustedCertificateRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<TrustedCertificate> findAllTrustedCertificate() {
        return this.repository.findAll();
    }

    @Override
    public TrustedCertificate saveTrustedCertificate(TrustedCertificate trustedCertificate) {
        return this.repository.save(trustedCertificate);
    }

    @Override
    public TrustedCertificate findByTrustedCertificateId(Long id) {
        return this.repository.findTrustedCertificateById(id);
    }

    @Override
    public TrustedCertificate updateTrustedCertificate(TrustedCertificate trustedCertificate) {
        return this.repository.save(trustedCertificate);
    }

    @Override
    public void deleteTrustedCertificate(Long id) {
        TrustedCertificate trustedCertificate = this.repository.findTrustedCertificateById(id);
        if (trustedCertificate == null) {
            throw new MchdException(String.format("Can`t delete Trusted Certificate because Trusted Certificate %s", NOT_FOUND));
        }
        this.repository.deleteById(id);
    }

    @Override
    public Integer countTrustedCertificateByImprint(String imprint){
        return this.repository.countTrustedCertificateByImprint(imprint);
    }
}
