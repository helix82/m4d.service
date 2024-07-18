package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import su.opencode.digsigcik.synapi.model.AcmeCertificate;
import su.opencode.digsigcik.synapi.model.AcmeDomain;
import su.opencode.digsigcik.synapi.model.AcmeOrder;
import su.opencode.digsigcik.synapi.model.AcmeProvider;
import su.opencode.digsigcik.synapi.repository.CertificateRepository;
import su.opencode.digsigcik.synapi.repository.DomainRepository;
import su.opencode.digsigcik.synapi.repository.ProviderRepository;
import su.opencode.digsigcik.synapi.service.AcmeService;
import su.opencode.digsigcik.synapi.util.CertUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
@Slf4j
public class CertificateController {
    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private AcmeService acmeService;

    @Operation(summary = "Получить список сертификатов указанного домена",
            description = "Возвращает список сертификатов указанного домена" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @GetMapping("/domains/{domainId}/certificates")
    public ResponseEntity<List<AcmeCertificate>> getAllCertificatesByDomainId(@PathVariable(value = "domainId") Long domainId) {
        if (!domainRepository.existsById(domainId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found AcmeDomain with id = " + domainId);
        }
        List<AcmeCertificate> certificates = certificateRepository.findByDomainId(domainId);
        return new ResponseEntity<>(certificates, HttpStatus.OK);
    }

    @Operation(summary = "Получить список сертификатов указанного провайдера",
            description = "Возвращает список сертификатов указанного провайдера" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @GetMapping("/providers/{providerId}/certificates")
    public ResponseEntity<List<AcmeCertificate>> getAllCertificatesByProviderId(@PathVariable(value = "providerId") Long providerId) {
        if (!providerRepository.existsById(providerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found AcmeProvider with id = " + providerId);
        }
        List<AcmeCertificate> certificates = certificateRepository.findByProviderId(providerId);
        return new ResponseEntity<>(certificates, HttpStatus.OK);
    }

    public static Map certInfo(AcmeCertificate certificate) {
        HashMap<String, Object> ret = new HashMap<>();
        if(certificate.getDeleted()) {
            ret.put("status", "deleted");
        } else {
            if(certificate.getRevoked()) {
                ret.put("status", "revoked");
            }
        }

        ret.put("id", certificate.getId());
        ret.put("deleted", certificate.getDeleted());
        ret.put("revoked", certificate.getRevoked());
        ret.put("data", certificate.getData());
        AcmeProvider provider = certificate.getProvider();
        if(provider!=null) {
            ret.put("provider", String.valueOf(provider.getName()));
            ret.put("providerId", String.valueOf(provider.getId()));
        }
        AcmeDomain domain = certificate.getDomain();
        if(domain!=null) {
            ret.put("domain", String.valueOf(domain.getDomainUrl()));
            ret.put("domainId", String.valueOf(domain.getId()));
        }

        AcmeOrder order = certificate.getOrder();
        if(order!=null) {
            ret.put("orderId", String.valueOf(order.getId()));
        }

        try {
            ret.put("status", "unknown");
            ret.putAll(CertUtils.certInfo(certificate.getContent()));
        } catch (CertificateException e) {
            ret.put("error", "invalid certificate");
            ret.put("status", "error");
        }
        return ret;
    }

    @Operation(summary = "Получить список все сертификатов",
            description = "Возвращает список всех сертификатов" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @GetMapping("/certificates")
    public ResponseEntity<List<Map>> getAllCertificates() {
        ArrayList<Map> list = new ArrayList<>();
        List<AcmeCertificate> certificates = certificateRepository.findAll();
        for(AcmeCertificate certificate:certificates) {
            if(!certificate.getDeleted()) {
                list.add(certInfo(certificate));
            }
        }
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @Operation(summary = "Получить указанный сертификат",
            description = "Возвращает указанный сертификат" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @GetMapping("/certificates/{id}")
    public ResponseEntity<AcmeCertificate> getCertificateBylId(@PathVariable(value = "id") Long id) {
        AcmeCertificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found AcmeCertificate with id = " + id));

        return new ResponseEntity<>(certificate, HttpStatus.OK);
    }

    @Operation(summary = "Получить расширенную информацю о сертификате",
            description = "Возвращает расширенную информацю о сертификате" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @GetMapping("/certificates/{id}/info")
    public ResponseEntity<Map> getCertificateInfoBylId(HttpServletResponse httpResponse,
                                                            @PathVariable(value = "id") Long id) {
        AcmeCertificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found AcmeCertificate with id = " + id));
        return new ResponseEntity<>(certInfo(certificate), HttpStatus.OK);
    }

    @Operation(summary = "Выпустить новый сертификат в автоматическом режиме",
            description = "Выпускает новый сертификат в автоматическом режиме" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Сертификат успешно выпущен" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @PostMapping("/certificates")
    public ResponseEntity<Map> certificateReleaseAuto(HttpSession httpSession, HttpServletRequest httpRequest,
                                            @RequestParam(value = "provider") Long providerId,
                                            @RequestParam(value = "domain") String domain,
                                            @RequestParam(value = "notAfter") String notAfter ) {
        Map data = httpRequest.getParameterMap();
        data.remove("provider");
        data.remove("domain");
        data.remove("notAfter");
        HashMap<String, Object> out = new HashMap<>();
        data.forEach((key, value) -> {
            out.put((String)key, Arrays.stream((String[])value).findFirst().get());
        });
        return acmeService.certificate(httpSession,domain, notAfter, providerId, out);
    }

    @Operation(summary = "Отозвать сертификат",
            description = "Отзывает сертификат" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Сертификат успешно отозван" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @PostMapping("/certificates/{id}/revoke")
    public ResponseEntity<Map> revoke(HttpSession httpSession,
                      @PathVariable(value = "id") Long id
    ) {
        return acmeService.revoke(httpSession,id);
    }


    @Operation(summary = "Получить файл сертификата",
            description = "Возвращает файл сертификата" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @GetMapping("/certificates/{id}/download")
    public ResponseEntity<String> downloadCertificateBylId(HttpServletResponse httpResponse,
                                                                    @PathVariable(value = "id") Long id) {
        AcmeCertificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found AcmeCertificate with id = " + id));
        httpResponse.setContentType ("application/width-x509-user-cert");
        httpResponse.setHeader ("Content-Disposition", "attachment; filename="+certificate.getDomain().getDomainUrl()+"-chain.crt");
        return new ResponseEntity<>(certificate.getContent(), HttpStatus.OK);
    }

    @Operation(summary = "Удалить указанный сертификат",
            description = "Удаляет указанный сертификат" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно удалено" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @DeleteMapping("/certificates/{id}")
    public ResponseEntity<HttpStatus> deleteCertificate(@PathVariable("id") long id) {
        AcmeCertificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found AcmeCertificate with id = " + id));
        certificate.setDeleted(true);
        certificateRepository.save(certificate);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Удалить сертификаты для указанного домена",
            description = "Удаляет сертификаты для указанного домена" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно удалено" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @DeleteMapping("/domain/{domainId}/certificates")
    public ResponseEntity<List<AcmeCertificate>> deleteAllCertificatesOfDomain(@PathVariable(value = "domainId") Long domainId) {
        if (!domainRepository.existsById(domainId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found Domain with id = " + domainId);
        }
        List<AcmeCertificate> certificates = certificateRepository.findByDomainId(domainId);
        for(AcmeCertificate certificate: certificates) {
            certificate.setDeleted(true);
            certificateRepository.save(certificate);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Загрузить сертификат",
            description = "Загружает сертификат" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно загружен и создан" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @PostMapping(value = "/certificate/upload")
    public ResponseEntity<AcmeCertificate> uploadCertificate(
            @RequestParam(value = "provider", required = false) @Parameter(name = "provider", description="Поставщик сертификата", example = "provider") Long provider,
            @RequestParam(value = "domain", required = false) @Parameter(name = "domain", description="Домен", example = "domain") Long domain,
            @RequestPart("content") @Parameter(name = "content", description="Содержимое сертификата", example = "", required = true) MultipartFile content
    ) {
        try {
            AcmeProvider _provider = providerRepository.findById(provider)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found AcmeProvider with id = " + provider));

            AcmeDomain _domain = domainRepository.findById(domain)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found AcmeDomain with id = " + domain));


            String certContent = new String(content.getBytes());
            Map certInfo = CertUtils.certInfo(certContent);

            AcmeCertificate acmeCertificate = new AcmeCertificate(_domain, certContent);
            if(certInfo.containsKey("notAfter")) acmeCertificate.setNotAfter((Date)(certInfo.get("notAfter")));
            if(certInfo.containsKey("notBefore")) acmeCertificate.setNotBefore((Date)(certInfo.get("notBefore")));
            acmeCertificate.setProvider(_provider);

            HashMap<String,Object> data = new HashMap<>();
            data.put("fileName", content.getOriginalFilename());
            acmeCertificate.setData(data);

            certificateRepository.save(acmeCertificate);
            return new ResponseEntity<>(acmeCertificate, HttpStatus.CREATED);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't upload certificate");
        } catch (CertificateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid certificate");
        }
    }
}
