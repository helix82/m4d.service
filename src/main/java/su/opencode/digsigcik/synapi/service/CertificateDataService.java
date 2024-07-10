package su.opencode.digsigcik.synapi.service;

import su.opencode.digsigcik.synapi.model.ca.CertificateData;

import java.util.List;

public interface CertificateDataService {
    List<CertificateData> findAllCertificateData();
    CertificateData findCertificateDataByKeyId(String keyId);
    CertificateData saveCertificateData(CertificateData certificateData);
    CertificateData findByCertificateDataId(Long id);
    Integer countCertificateDataByImprint(String imprint);
    CertificateData updateCertificateData(CertificateData certificateData);
    void deleteCertificateData(Long id);
}
