package su.opencode.digsigcik.synapi.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.model.ca.CertificateData;
import su.opencode.digsigcik.synapi.repository.CertificateDataRepository;

@Service
@Primary
public class CertificateDataServiceImpl implements CertificateDataService{
    private final CertificateDataRepository repository;

    public CertificateDataServiceImpl(CertificateDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public void saveCertificateData(CertificateData certificateData) {
        this.repository.save(certificateData);
    }
}
