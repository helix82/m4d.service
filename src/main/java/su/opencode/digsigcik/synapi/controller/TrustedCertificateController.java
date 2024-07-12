package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.cryptacular.util.CertUtil;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.annotation.MchdExceptionHandler;
import su.opencode.digsigcik.synapi.dto.TrustedCertificateDTO;
import su.opencode.digsigcik.synapi.dto.TrustedCertificateListDTO;
import su.opencode.digsigcik.synapi.enums.AccreditationStatusEnum;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.model.TrustedCertificate;
import su.opencode.digsigcik.synapi.service.TrustedCertificateService;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static su.opencode.digsigcik.synapi.util.Constants.ID_IS_NULL;
import static su.opencode.digsigcik.synapi.util.Constants.*;

@RestController
@MchdExceptionHandler
@RequestMapping("/api/v1/trusted_certificate")
@AllArgsConstructor
public class TrustedCertificateController {
    private final TrustedCertificateService service;
    private final ModelMapper modelMapper;

    @Operation(summary = CONTROLLER_TRUSTED_CERTIFICATE_FIND_ALL_SUMMARY, description = CONTROLLER_TRUSTED_CERTIFICATE_FIND_ALL_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping(produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> findAllTrustedCertificate() {
        List<TrustedCertificate> findAll;
        try {
            findAll = service.findAllTrustedCertificate();
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(TRUSTED_CERTIFICATE_IS_NULL);
        }
        return new ResponseEntity<>(findAll.stream().map(this::convertToDtoList).collect(Collectors.toList()), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_TRUSTED_CERTIFICATE_SAVE_SUMMARY, description = CONTROLLER_TRUSTED_CERTIFICATE_SAVE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PostMapping(value = "save", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> saveTrustedCertificate(@RequestBody TrustedCertificateDTO trustedCertificateDTO) {
        TrustedCertificate trustedCertificate = convertToEntity(trustedCertificateDTO);
        TrustedCertificate save;
        try {
            save = service.saveTrustedCertificate(trustedCertificate);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(TRUSTED_CERTIFICATE_IS_NULL);
        }
        return new ResponseEntity<>(convertToDto(save), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_TRUSTED_CERTIFICATE_FIND_BY_ID_SUMMARY, description = CONTROLLER_TRUSTED_CERTIFICATE_FIND_BY_ID_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping(value = "/{id}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> findByTrustedCertificateId(@PathVariable @Parameter(name = "id",
            description = PARAMETER_TRUSTED_CERTIFICATE_ID_DESCRIPTION, example = "1" ) Long id) {
        TrustedCertificate findById;
        try {
            findById = service.findByTrustedCertificateId(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(TRUSTED_CERTIFICATE_IS_NULL);
        }
        return new ResponseEntity<>(convertToDto(findById), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_TRUSTED_CERTIFICATE_UPDATE_SUMMARY, description = CONTROLLER_TRUSTED_CERTIFICATE_UPDATE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PutMapping(value = "update", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> updateTrustedCertificate(@RequestBody TrustedCertificateDTO trustedCertificateDTO) {
        TrustedCertificate trustedCertificate = convertToEntity(trustedCertificateDTO);
        TrustedCertificate update;
        try {
            update = service.updateTrustedCertificate(trustedCertificate);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(TRUSTED_CERTIFICATE_IS_NULL);
        }
        return new ResponseEntity<>(convertToDto(update), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_TRUSTED_CERTIFICATE_DELETE_SUMMARY, description = CONTROLLER_TRUSTED_CERTIFICATE_DELETE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @DeleteMapping(value = "delete/{id}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> deleteTrustedCertificate(@PathVariable @Parameter(name = "id",
            description = PARAMETER_TRUSTED_CERTIFICATE_ID_DESCRIPTION, example = "1" ) Long id) {
        try {
            service.deleteTrustedCertificate(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(ID_IS_NULL);
        }
        Map<String, String> body = new HashMap<>();
        body.put("result", "true");

        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_TRUSTED_CERTIFICATE_UPLOAD_SUMMARY, description = CONTROLLER_TRUSTED_CERTIFICATE_UPLOAD_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PostMapping(value = "/upload", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> upload(@RequestPart("srcFile") MultipartFile sourceFile) throws IOException, CertificateEncodingException {
        String extension = FilenameUtils.getExtension(sourceFile.getOriginalFilename());
        if(!Objects.equals(extension, "cer")){
            throw new BadRequestException(TRUSTED_CERTIFICATE_UPLOAD_WRONG_FORMAT);
        }
        X509Certificate certificate = CertUtil.decodeCertificate(sourceFile.getBytes());
        TrustedCertificate trustedCertificate = new TrustedCertificate();
        trustedCertificate.setSerialNumber(certificate.getSerialNumber().toString());
        trustedCertificate.setDateStart(convertRFC1123ToIsoInstant(certificate.getNotBefore().toString()));
        trustedCertificate.setDateEnd(convertRFC1123ToIsoInstant(certificate.getNotAfter().toString()));
        trustedCertificate.setTypeInput(CONTROLLER_CA_TYPE_INPUT_AUTO);
        trustedCertificate.setAccreditationStatus(AccreditationStatusEnum.VALID.name());
        trustedCertificate.setCertificate(new String(sourceFile.getBytes()));
        trustedCertificate.setImprint(DigestUtils.sha1Hex(certificate.getEncoded()));
        trustedCertificate.setKeyId(CertUtil.subjectKeyId(certificate).replace(":", ""));
        var substr = certificate.getIssuerDN().getName().split(", ");
        for(String s : substr) {
            if(s.length()>2){
                var st = s.substring(0,3);
                if (st.equals("CN="))
                {
                    trustedCertificate.setIssuedTo(s.substring(3));
                    break;
                }
            }
        }
        var existsCert = service.countTrustedCertificateByImprint(trustedCertificate.getImprint());
        if(existsCert > 0){
            throw new BadRequestException(TRUSTED_CERTIFICATE_UPLOAD_EXISTS);
        }
        try {
            service.saveTrustedCertificate(trustedCertificate);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(TRUSTED_CERTIFICATE_IS_NULL);
        }

        Map<String, String> body = new HashMap<>();
        body.put("imported", "1");

        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    private TrustedCertificateDTO convertToDto(TrustedCertificate trustedCertificate){
        TrustedCertificateDTO trustedCertificateDTO = modelMapper.map(trustedCertificate, TrustedCertificateDTO.class);
        return trustedCertificateDTO;
    }

    private TrustedCertificateListDTO convertToDtoList(TrustedCertificate trustedCertificate){
        TrustedCertificateListDTO trustedCertificateDTO = modelMapper.map(trustedCertificate, TrustedCertificateListDTO.class);
        return trustedCertificateDTO;
    }

    private TrustedCertificate convertToEntity(TrustedCertificateDTO trustedCertificateDTO){
        TrustedCertificate trustedCertificate = modelMapper.map(trustedCertificateDTO, TrustedCertificate.class);
        return trustedCertificate;
    }

    private String convertRFC1123ToIsoInstant(String date){
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
        Instant instant1 = formatter.parse(date, Instant::from);
        return instant1.toString();
    }
}
