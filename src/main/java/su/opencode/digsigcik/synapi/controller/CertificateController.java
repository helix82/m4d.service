package su.opencode.digsigcik.synapi.controller;

import lombok.AllArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import su.opencode.digsigcik.synapi.model.AcmeAccount;
import su.opencode.digsigcik.synapi.model.AcmeCertificate;
import su.opencode.digsigcik.synapi.model.AcmeDomain;
import su.opencode.digsigcik.synapi.repository.AccountRepository;
import su.opencode.digsigcik.synapi.repository.CertificateRepository;
import su.opencode.digsigcik.synapi.repository.DomainRepository;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
public class CertificateController {
    @Autowired
    private DomainRepository domainRepository;


    @Autowired
    private CertificateRepository certificateRepository;

    @GetMapping("/domains/{domainId}/certificates")
    public ResponseEntity<List<AcmeCertificate>> getAllCertificatesByDomainId(@PathVariable(value = "domainId") Long domainId) {
        if (!domainRepository.existsById(domainId)) {
            throw new ResourceNotFoundException("Not found AcmeDomain with id = " + domainId);
        }
        List<AcmeCertificate> certificates = certificateRepository.findByDomainId(domainId);
        return new ResponseEntity<>(certificates, HttpStatus.OK);
    }


    private Map certInfo(AcmeCertificate certificate) {
        HashMap<String, Object> ret = new HashMap<>();

        CertificateFactory fac = null;
        try {
            fac = CertificateFactory.getInstance("X509");
            AcmeDomain domain = certificate.getDomain();
            X509Certificate cert = (X509Certificate) fac.generateCertificate(new ByteArrayInputStream(certificate.getContent().getBytes()));

            ret.put("id", certificate.getId());

            ret.put("deleted", certificate.getDeleted());
            ret.put("revoked", certificate.getRevoked());

            ret.put("issuerDN", String.valueOf(cert.getIssuerDN()));
            ret.put("type", String.valueOf(cert.getType()));
            ret.put("domain", String.valueOf(domain.getDomainUrl()));
            ret.put("algName", String.valueOf(cert.getSigAlgName()));
            ret.put("notBefore", cert.getNotBefore());
            ret.put("notAfter", cert.getNotAfter());
            ret.put("serialNumber", String.valueOf(cert.getSerialNumber()));

        } catch (CertificateException e) {
            e.printStackTrace();
        }


        return ret;
    }

    @GetMapping("/certificates")
    public ResponseEntity<List<Map>> getAllCertificates() {
        ArrayList<Map> list = new ArrayList<>();
        List<AcmeCertificate> certificates = certificateRepository.findAll();
        for(AcmeCertificate certificate:certificates) {
            list.add(certInfo(certificate));
        }
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/certificates/{id}")
    public ResponseEntity<AcmeCertificate> getCertificateBylId(@PathVariable(value = "id") Long id) {
        AcmeCertificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found AcmeCertificate with id = " + id));

        return new ResponseEntity<>(certificate, HttpStatus.OK);
    }

    @GetMapping("/certificates/{id}/info")
    public ResponseEntity<Map> getCertificateInfoBylId(HttpServletResponse httpResponse,
                                                            @PathVariable(value = "id") Long id) {
        AcmeCertificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found AcmeCertificate with id = " + id));
        return new ResponseEntity<>(certInfo(certificate), HttpStatus.OK);
    }

    @GetMapping("/certificates/{id}/download")
    public ResponseEntity<String> downloadCertificateBylId(HttpServletResponse httpResponse,
                                                                    @PathVariable(value = "id") Long id) {
        AcmeCertificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found AcmeCertificate with id = " + id));
        httpResponse.setContentType ("application/x-x509-user-cert");
        httpResponse.setHeader ("Content-Disposition", "attachment; filename="+certificate.getDomain().getDomainUrl()+"-chain.crt");
        return new ResponseEntity<>(certificate.getContent(), HttpStatus.OK);
    }

    @DeleteMapping("/certificates/{id}")
    public ResponseEntity<HttpStatus> deleteCertificate(@PathVariable("id") long id) {
        AcmeCertificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found AcmeCertificate with id = " + id));
        certificate.setDeleted(true);
        certificateRepository.save(certificate);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/domain/{domainId}/certificates")
    public ResponseEntity<List<AcmeCertificate>> deleteAllCertificatesOfDomain(@PathVariable(value = "domainId") Long domainId) {
        if (!domainRepository.existsById(domainId)) {
            throw new ResourceNotFoundException("Not found Domain with id = " + domainId);
        }
        List<AcmeCertificate> certificates = certificateRepository.findByDomainId(domainId);
        for(AcmeCertificate certificate: certificates) {
            certificate.setDeleted(true);
            certificateRepository.save(certificate);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
