package su.opencode.digsigcik.synapi.service;

import su.opencode.digsigcik.synapi.model.ca.CAXml;

public interface CertificateDataService {
    void saveCertificateData(CAXml.CA.SHSystems.SHSystem.KeysAuthPersons.KeyAuthPerson.Certificates.CertificateData certificateData);
}
