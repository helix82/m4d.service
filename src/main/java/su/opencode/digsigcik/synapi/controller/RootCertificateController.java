package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.model.RootCertificate;
import su.opencode.digsigcik.synapi.repository.RootCertificateRepository;
import su.opencode.digsigcik.synapi.util.CertUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
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
public class RootCertificateController {

    @Autowired
    private RootCertificateRepository rootCertificateRepository;

    public static Map certInfo(RootCertificate certificate) {
        HashMap<String, Object> ret = new HashMap<>();

        if(certificate.getContent() != null) {
            try {
                ret.put("id", certificate.getId());
                ret.put("owner", certificate.getOwnerName());
                ret.put("deleted", certificate.getDeleted());
                ret.put("revoked", certificate.getRevoked());

                ret.putAll(CertUtils.certInfo(certificate.getContent()));
            } catch (CertificateException e) {
                ret.put("error", "invalid certificate");
            }
        } else {
            ret.put("id", certificate.getId());
            ret.put("owner", certificate.getOwnerName());
            ret.put("deleted", certificate.getDeleted());
            ret.put("revoked", certificate.getRevoked());
        }
        return ret;
    }

    @Operation(summary = "Получить список корневых сертификатов",
            description = "Возвращает список корневых сертификатов" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @GetMapping("/root/certificates")
    public ResponseEntity<List<Map>> getAllRootCertificates() {
        ArrayList<Map> list = new ArrayList<>();
        List<RootCertificate> certificates = rootCertificateRepository.findAll();
        for(RootCertificate certificate:certificates) {
            if(!certificate.getDeleted()){
                list.add(certInfo(certificate));
            }
        }
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @Operation(summary = "Получить указанный корневой сертификат",
            description = "Возвращает указанный корневой сертификат" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @GetMapping("/root/certificates/{id}")
    public ResponseEntity<RootCertificate> getRootCertificateBylId(@PathVariable(value = "id") Long id) {
        RootCertificate certificate = rootCertificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found RootCertificate with id = " + id));

        return new ResponseEntity<>(certificate, HttpStatus.OK);
    }

    @Operation(summary = "Получить расширенную информацию о корневой сертификате",
            description = "Возвращает расширенную информацию о корневой  сертификате" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @GetMapping("/root/certificates/{id}/info")
    public ResponseEntity<Map> getRootCertificateInfoBylId(@PathVariable(value = "id") Long id) {
        RootCertificate certificate = rootCertificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found RootCertificate with id = " + id));
        return new ResponseEntity<>(certInfo(certificate), HttpStatus.OK);
    }

    @Operation(summary = "Создать корневой сертификат",
            description = "Создаёт корневой сертификат" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно создано" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @PostMapping("/root/certificates")
    public ResponseEntity<RootCertificate> saveRootCertificate(@RequestBody RootCertificate certificate) {
        RootCertificate _certificate  = new RootCertificate(certificate.getOwnerName(), certificate.getContent());
        if(certificate.getDeleted()!=null)_certificate.setDeleted(certificate.getDeleted());
        if(certificate.getRevoked()!=null)_certificate.setRevoked(certificate.getRevoked());
        return new ResponseEntity<>(rootCertificateRepository.save(_certificate), HttpStatus.CREATED);
    }

    @Operation(summary = "Загрузить и создать корневой сертификат",
            description = "Загружает и создайт корневой сертификат" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно загружен и создан" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @PostMapping("/root/certificates/upload")
    public ResponseEntity<RootCertificate> uploadAndCreateRootCertificate(
            @RequestPart(value = "issuer", required = false) @Parameter(name = "issuer", description="Наименование владельца", example = "issuer") String issuer,
            @RequestPart("content") @Parameter(name = "content", description="Содержимое корневого сертификата", example = "", required = true) MultipartFile content
    ) {
        try {
            String certContent = new String(content.getBytes());
            Map certInfo = CertUtils.certInfo(certContent);
            String _issuer = (String)(certInfo.get("cn"));
            if(issuer!=null)_issuer=issuer;
            RootCertificate _certificate = new RootCertificate(_issuer, certContent);
            return new ResponseEntity<>(rootCertificateRepository.save(_certificate), HttpStatus.CREATED);
        } catch (IOException e) {
            throw new ResourceNotFoundException("Can't upload certificate");
        } catch (CertificateException e) {
            throw new ResourceNotFoundException("Invalid certificate");
        }
    }


    @Operation(summary = "Обновить корневой сертификат",
            description = "Обновляет корневой сертификат" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно обновлено" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @PutMapping("/root/certificates/{id}")
    public ResponseEntity<RootCertificate> updateRootCertificate(@PathVariable Long id, @RequestBody RootCertificate certificate) {
        RootCertificate _certificate = rootCertificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found RootCertificate with id = " + id));

        if(certificate.getOwnerName()!=null)_certificate.setOwnerName(certificate.getOwnerName());
        if(certificate.getContent()!=null)_certificate.setContent(certificate.getContent());
        if(certificate.getDeleted()!=null)_certificate.setDeleted(certificate.getDeleted());
        if(certificate.getRevoked()!=null)_certificate.setRevoked(certificate.getRevoked());
        return new ResponseEntity<>(rootCertificateRepository.save(_certificate), HttpStatus.OK);
    }

    @Operation(summary = "Получить файл корневого сертификата",
            description = "Возвращает файл корневого сертификата" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @GetMapping("/root/certificates/{id}/download")
    public ResponseEntity<String> downloadRootCertificateBylId(HttpServletResponse httpResponse,
                                                                    @PathVariable(value = "id") Long id) {
        RootCertificate certificate = rootCertificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found RootCertificate with id = " + id));
        httpResponse.setContentType ("application/width-x509-user-cert");
        httpResponse.setHeader ("Content-Disposition", "attachment; filename=root_ca.crt");
        return new ResponseEntity<>(certificate.getContent(), HttpStatus.OK);
    }

    @Operation(summary = "Загрузить файл корневого сертификата",
            description = "Загружает файл корневого сертификата" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @PostMapping("/root/certificates/{id}/upload")
    public ResponseEntity<RootCertificate> uploadRootCertificateBylId(@PathVariable(value = "id") Long id,
           @RequestPart("content") @Parameter(name = "content", description="Содержимое корневого сертификата", example = "", required = true) MultipartFile content
    ) {
        RootCertificate certificate = rootCertificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found RootCertificate with id = " + id));
        try {
            certificate.setContent( new String(content.getBytes()));
        } catch (IOException e) {
            throw new ResourceNotFoundException("Can't upload certificate");
        }
        rootCertificateRepository.save(certificate);
        return new ResponseEntity<>(certificate, HttpStatus.OK);
    }

    @Operation(summary = "Удалить указанный корневой сертификат",
            description = "Удаляет указанный корневой сертификат" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно удалено" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @DeleteMapping("/root/certificates/{id}")
    public ResponseEntity<HttpStatus> deleteCertificate(@PathVariable("id") long id) {
        RootCertificate certificate = rootCertificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found RootCertificate with id = " + id));
        certificate.setDeleted(true);
        rootCertificateRepository.save(certificate);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
