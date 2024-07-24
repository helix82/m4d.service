package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.Extension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.annotation.MchdExceptionHandler;
import su.opencode.digsigcik.synapi.dto.RevokedCertificateDTO;
import su.opencode.digsigcik.synapi.dto.RevokedCertificateListDTO;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.model.RevokedCertificate;
import su.opencode.digsigcik.synapi.service.RevokedCertificateService;

import javax.validation.ConstraintViolationException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static su.opencode.digsigcik.synapi.util.Constants.*;

@RestController
@MchdExceptionHandler
@RequestMapping("/api/v1/revoked_certificate")
@AllArgsConstructor
public class RevokedCertificateController {
    private final RevokedCertificateService service;
    private final ModelMapper modelMapper;

    @Operation(summary = CONTROLLER_REVOKED_CERTIFICATE_FIND_ALL_SUMMARY, description = CONTROLLER_REVOKED_CERTIFICATE_FIND_ALL_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping(produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> findAllRevokedCertificate() {
        List<RevokedCertificate> findAll;
        try {
            findAll = service.findAllRevokedCertificate();
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(REVOKED_CERTIFICATE_IS_NULL);
        }
        return new ResponseEntity<>(findAll.stream().map(this::convertToDtoList).collect(Collectors.toList()), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_REVOKED_CERTIFICATE_SAVE_SUMMARY, description = CONTROLLER_REVOKED_CERTIFICATE_SAVE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PostMapping(value = "save", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> saveRevokedCertificate(@RequestBody RevokedCertificateDTO revokedCertificateDTO) {
        RevokedCertificate revokedCertificate = convertToEntity(revokedCertificateDTO);
        RevokedCertificate save;
        try {
            save = service.saveRevokedCertificate(revokedCertificate);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(REVOKED_CERTIFICATE_IS_NULL);
        }
        return new ResponseEntity<>(convertToDto(save), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_REVOKED_CERTIFICATE_FIND_BY_ID_SUMMARY, description = CONTROLLER_REVOKED_CERTIFICATE_FIND_BY_ID_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping(value = "/{id}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> findByRevokedCertificateId(@PathVariable @Parameter(name = "id",
            description = PARAMETER_REVOKED_CERTIFICATE_ID_DESCRIPTION, example = "1" ) Long id) {
        RevokedCertificate findById;
        try {
            findById = service.findByRevokedCertificateId(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(REVOKED_CERTIFICATE_IS_NULL);
        }
        return new ResponseEntity<>(convertToDto(findById), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_REVOKED_CERTIFICATE_UPDATE_SUMMARY, description = CONTROLLER_REVOKED_CERTIFICATE_UPDATE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PutMapping(value = "update", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> updateRevokedCertificate(@RequestBody RevokedCertificateDTO revokedCertificateDTO) {
        RevokedCertificate revokedCertificate = convertToEntity(revokedCertificateDTO);
        RevokedCertificate update;
        try {
            update = service.updateRevokedCertificate(revokedCertificate);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(REVOKED_CERTIFICATE_IS_NULL);
        }
        return new ResponseEntity<>(convertToDto(update), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_REVOKED_CERTIFICATE_DELETE_SUMMARY, description = CONTROLLER_REVOKED_CERTIFICATE_DELETE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @DeleteMapping(value = "delete/{id}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> deleteRevokedCertificate(@PathVariable @Parameter(name = "id",
            description = PARAMETER_REVOKED_CERTIFICATE_ID_DESCRIPTION, example = "1" ) Long id) {
        try {
            service.deleteRevokedCertificate(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(ID_IS_NULL);
        }
        Map<String, String> body = new HashMap<>();
        body.put("result", "true");

        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_REVOKED_CERTIFICATE_UPLOAD_SUMMARY, description = CONTROLLER_REVOKED_CERTIFICATE_UPLOAD_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PostMapping(value = "/upload", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> upload(@RequestPart("srcFile") MultipartFile sourceFile) throws IOException, CertificateException, CRLException {
        String extension = FilenameUtils.getExtension(sourceFile.getOriginalFilename());
        if (!"crl".equals(extension)) {
            throw new BadRequestException(REVOKED_CERTIFICATE_UPLOAD_WRONG_FORMAT);
        }

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509CRL crl;
        try (BufferedInputStream bis = new BufferedInputStream(sourceFile.getInputStream())) {
            crl = (X509CRL) cf.generateCRL(bis);
        }

        RevokedCertificate revokedCertificate = new RevokedCertificate();
        String[] issuer = crl.getIssuerDN().getName().split(", ");
        for (String s : issuer) {
            if (s.startsWith("CN=")) {
                revokedCertificate.setIssuedTo(s.substring(3));
            }
        }
        revokedCertificate.setOriginalFilename(sourceFile.getOriginalFilename());
        revokedCertificate.setDateEnd(convertRFC1123ToIsoInstant(crl.getNextUpdate().toString()));
        revokedCertificate.setDateStart(convertRFC1123ToIsoInstant(crl.getThisUpdate().toString()));

        String oid = Extension.authorityKeyIdentifier.getId();
        byte[] extValue = crl.getExtensionValue(oid);
        if (extValue != null) {
            ASN1OctetString akiOc = ASN1OctetString.getInstance(extValue);
            AuthorityKeyIdentifier aki = AuthorityKeyIdentifier.getInstance(akiOc.getOctets());
            String keyIdIssuer = new DEROctetString(aki.getKeyIdentifier()).toString().substring(1);
            revokedCertificate.setKeyId(keyIdIssuer);
        }

        revokedCertificate.setImprint(DigestUtils.sha1Hex(crl.getEncoded()));
        revokedCertificate.setCertificate(Base64.getEncoder().encodeToString(sourceFile.getBytes()));

        if (service.countRevokedCertificateByImprint(revokedCertificate.getImprint()) > 0) {
            throw new BadRequestException(REVOKED_CERTIFICATE_UPLOAD_EXISTS);
        }

        RevokedCertificate response;
        try {
            response = service.saveRevokedCertificate(revokedCertificate);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(REVOKED_CERTIFICATE_IS_NULL);
        }

        return new ResponseEntity<>(convertToDto(response), HttpStatus.OK);
    }



    private RevokedCertificateDTO convertToDto(RevokedCertificate revokedCertificate){
        RevokedCertificateDTO revokedCertificateDTO = modelMapper.map(revokedCertificate, RevokedCertificateDTO.class);
        return revokedCertificateDTO;
    }

    private RevokedCertificateListDTO convertToDtoList(RevokedCertificate revokedCertificate){
        RevokedCertificateListDTO revokedCertificateDTO = modelMapper.map(revokedCertificate, RevokedCertificateListDTO.class);
        return revokedCertificateDTO;
    }

    private RevokedCertificate convertToEntity(RevokedCertificateDTO revokedCertificateDTO){
        RevokedCertificate revokedCertificate = modelMapper.map(revokedCertificateDTO, RevokedCertificate.class);
        return revokedCertificate;
    }

    private static String convertRFC1123ToIsoInstant(String date){
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
        Instant instant1 = formatter.parse(date, Instant::from);
        return instant1.toString();
    }
}
