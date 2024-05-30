package su.opencode.digsigcik.synapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import su.opencode.digsigcik.synapi.model.ca.CAXml;

@Repository
public interface CertificateDataRepository extends JpaRepository<CAXml.CA.SHSystems.SHSystem.KeysAuthPersons.KeyAuthPerson.Certificates.CertificateData, Long> {

}
