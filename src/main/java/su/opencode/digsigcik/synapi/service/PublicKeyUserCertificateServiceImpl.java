package su.opencode.digsigcik.synapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.Extension;
import org.cryptacular.StreamException;
import org.cryptacular.util.CertUtil;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ru.CryptoPro.ssl.util.HexUtil;
import su.opencode.digsigcik.synapi.enums.TypeUserCertificateEnum;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.exception.MchdException;
import su.opencode.digsigcik.synapi.model.PublicKeyUserCertificate;
import su.opencode.digsigcik.synapi.model.ca.CertificateData;
import su.opencode.digsigcik.synapi.repository.PublicKeyUserCertificateRepository;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;

import static su.opencode.digsigcik.synapi.util.Constants.*;

@Service
@Slf4j
@Primary
@RequiredArgsConstructor
public class PublicKeyUserCertificateServiceImpl implements PublicKeyUserCertificateService{
    private final PublicKeyUserCertificateRepository repository;
    private final CertificateDataService certificateDataService;

    @Override
    public List<PublicKeyUserCertificate> findAllPublicKeyUserCertificate() {
        return this.repository.findAll();
    }

    @Override
    public List<PublicKeyUserCertificate> findAllPublicKeyUserCertificateByLogin(String login) {
        return this.repository.findPublicKeyUserCertificateByLogin(login);
    }

    @Override
    public PublicKeyUserCertificate savePublicKeyUserCertificate(@NotNull PublicKeyUserCertificate publicKeyUserCertificate) {
        return this.repository.save(publicKeyUserCertificate);
    }

    @Override
    public PublicKeyUserCertificate findByPublicKeyUserCertificateId(@NotNull Long id) {
        PublicKeyUserCertificate publicKeyUserCertificate = this.repository.findPublicKeyUserCertificateById(id);
        if (publicKeyUserCertificate == null) {
            throw new MchdException(String.format("User certificate %s", NOT_FOUND));
        }
        return publicKeyUserCertificate;
    }

    @Override
    public PublicKeyUserCertificate updatePublicKeyUserCertificate(@NotNull PublicKeyUserCertificate publicKeyUserCertificate) {
        Long publicKeyUserCertificateId = publicKeyUserCertificate.getId();
        if (publicKeyUserCertificateId == null) {
            throw new BadRequestException(String.format("Can`t update user certificate because id %s!", NULL));
        }

        PublicKeyUserCertificate existPublicKeyUserCertificate = this.repository.findPublicKeyUserCertificateById(publicKeyUserCertificateId);
        if (existPublicKeyUserCertificate == null) {
            throw new MchdException(String.format("Can`t update. User certificate with id=%d %s", publicKeyUserCertificateId, NOT_FOUND));
        }
        return this.repository.save(publicKeyUserCertificate);
    }

    @Override
    public Integer countPublicKeyUserCertificateByImprint(String imprint){
        return this.repository.countPublicKeyUserCertificateByImprint(imprint);
    }

    public void checkQualifications() throws IOException {
        List<PublicKeyUserCertificate> publicKeyUserCertificateList = repository.findAll();
        for(PublicKeyUserCertificate c: publicKeyUserCertificateList){
            TypeUserCertificateEnum typeCert = TypeUserCertificateEnum.NOT_QUALIFIED;
            var substr = c.getIssuerWho().split(", ");
            String cnIssuer = "";
            for(String s : substr) {
                if(s.length()>2) {
                    if (s.startsWith("CN=")) {
                        cnIssuer = s.substring(3);
                    }
                }
            }

            String content = new String(c.getCertificate().getBytes(), StandardCharsets.UTF_8);
            X509Certificate certificate;
            if(!content.startsWith("-----BEGIN CERTIFICATE-----")){
                try {
                    String certHeader = "-----BEGIN CERTIFICATE-----\r\n";
                    String certFooter = "\r\n-----END CERTIFICATE-----";
                    String LINE_SEPARATOR = System.lineSeparator();
                    byte[] certBytes = c.getCertificate().getBytes();
                    String cert = certHeader + LINE_SEPARATOR + new String(certBytes) + LINE_SEPARATOR + certFooter;
                    InputStream stream = IOUtils.toInputStream(cert);
                    certificate = CertUtil.decodeCertificate(stream.readAllBytes());
                } catch (StreamException ignored) {
                    String encodedString = Base64.getEncoder().encodeToString(c.getCertificate().getBytes());
                    String certHeader = "-----BEGIN CERTIFICATE-----\r\n";
                    String certFooter = "\r\n-----END CERTIFICATE-----";
                    byte[] certBytes = encodedString.getBytes();
                    String cert = certHeader + new String(certBytes) + certFooter;
                    InputStream stream = IOUtils.toInputStream(cert);
                    certificate = CertUtil.decodeCertificate(stream.readAllBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                certificate = CertUtil.decodeCertificate(c.getCertificate().getBytes());//.getInputStream().readAllBytes());
            }

            String oid1 = Extension.authorityKeyIdentifier.getId();
            byte[] extValue1 = certificate.getExtensionValue(oid1);
            if (extValue1 == null) {
                throw new BadRequestException(PUBLIC_KEY_USER_CERTIFICATE_IS_NOT);
            }
            ASN1OctetString akiOc1 = ASN1OctetString.getInstance(extValue1);
            AuthorityKeyIdentifier aki1 = AuthorityKeyIdentifier.getInstance(akiOc1.getOctets());
            String keyIdIssuer = new DEROctetString(aki1.getKeyIdentifier()).toString().substring(1);
            String serialNumberIssuer = null;
            if(aki1.getAuthorityCertSerialNumber().toByteArray() != null) {
                serialNumberIssuer = HexUtil.toString(aki1.getAuthorityCertSerialNumber().toByteArray()).replace(":", "");
            }

            List<CertificateData> certificateDataList;
            try {
                certificateDataList = certificateDataService.findCertificateDataByKeyId(keyIdIssuer);
                long i = certificateDataList.size();//если больше одной записи, то, если сериалНумбер не пустой, перебираем и ищем ту что с нужным сериалНумбер
                if(certificateDataList != null){
                    for(CertificateData certificateData: certificateDataList) {
                        if(certificateData.getSerialNumber().equalsIgnoreCase(serialNumberIssuer)) {
                            var certIssuer = certificateData.getIssuedTo().replace("\\", "").replace("\"", "").toLowerCase();
                            var cnIssuerLC = cnIssuer.replace("\\", "").replace("\"", "").toLowerCase();
                            if (certIssuer.equals(cnIssuerLC)) {
                                typeCert = TypeUserCertificateEnum.QUALIFIED;
                            }
                        }
                    }
                }
            } catch (ConstraintViolationException e) {
                typeCert = TypeUserCertificateEnum.NOT_QUALIFIED;
            }
            if(!c.getType().equals(typeCert)){
                c.setType(typeCert);
                try {
                    repository.save(c);
                } catch (ConstraintViolationException e) {
                    log.warn("Не удалось изменить тип квалификации сертификата пользователя с ID=" + c.getId() + ": {}",e.getMessage());
                }
            }
        }
    }

}
