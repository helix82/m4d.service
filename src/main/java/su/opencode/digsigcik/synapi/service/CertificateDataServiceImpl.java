package su.opencode.digsigcik.synapi.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.model.ca.CertificateData;
import su.opencode.digsigcik.synapi.repository.CertificateDataRepository;

import java.util.List;

@Service
@Primary
public class CertificateDataServiceImpl implements CertificateDataService{
    private final CertificateDataRepository repository;

    public CertificateDataServiceImpl(CertificateDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public CertificateData saveCertificateData(CertificateData certificateData) {
        return this.repository.save(certificateData);
    }

    @Override
    public List<CertificateData> findAllCertificateData() {
        return this.repository.findAll();
    }

    @Override
    public CertificateData findByCertificateDataId(Long id) {
        return this.repository.findCertificateDataById(id);
    }

    @Override
    public CertificateData updateCertificateData(CertificateData certificateData) {
        return this.repository.save(certificateData);
    }

    @Override
    public void deleteCertificateData(Long id) {
        this.repository.deleteById(id);
    }
}
