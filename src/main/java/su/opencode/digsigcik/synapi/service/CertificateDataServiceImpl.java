package su.opencode.digsigcik.synapi.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.model.ca.CAXml;
import su.opencode.digsigcik.synapi.repository.CertificateDataRepository;

@Service
@Primary
public class CertificateDataServiceImpl implements CertificateDataService{
    private final CertificateDataRepository repository;

    public CertificateDataServiceImpl(CertificateDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public void saveCertificateData(CAXml.CA.SHSystems.SHSystem.KeysAuthPersons.KeyAuthPerson.Certificates.CertificateData certificateData) {
        this.repository.save(certificateData);
    }
}
