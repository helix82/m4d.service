package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.cryptacular.util.CertUtil;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.annotation.MchdExceptionHandler;
import su.opencode.digsigcik.synapi.dto.PublicKeyUserCertificateDto;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.model.PublicKeyUserCertificate;
import su.opencode.digsigcik.synapi.service.PublicKeyUserCertificateService;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static su.opencode.digsigcik.synapi.util.Constants.*;


@RestController
@MchdExceptionHandler
@RequestMapping("/api/v1/user_certificate")
@AllArgsConstructor
public class PublicKeyUserCertificateController {
    private final PublicKeyUserCertificateService service;
    private final ModelMapper modelMapper;

    @Operation(summary = CONTROLLER_PUBLIC_KEY_USER_CERTIFICATE_FIND_ALL_SUMMARY, description = CONTROLLER_PUBLIC_KEY_USER_CERTIFICATE_FIND_ALL_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping()
    public ResponseEntity<?> findAllPublicKeyUserCertificate() {
        List<PublicKeyUserCertificate> response;
        try {
            response = service.findAllPublicKeyUserCertificate();
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PUBLIC_KEY_USER_CERTIFICATE_IS_NULL);
        }
        return new ResponseEntity<>(response.stream().map(this::convertToDto).collect(Collectors.toList()), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_PUBLIC_KEY_USER_CERTIFICATE_SAVE_SUMMARY, description = CONTROLLER_PUBLIC_KEY_USER_CERTIFICATE_SAVE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PostMapping(value = "save", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> savePublicKeyUserCertificate(@RequestPart String login, @RequestPart("srcFile") MultipartFile sourceFile) throws IOException, CertificateException, NoSuchProviderException {
        String extension = FilenameUtils.getExtension(sourceFile.getOriginalFilename());
        PublicKeyUserCertificate publicKeyUserCertificate = new PublicKeyUserCertificate();
        if(Objects.equals(extension, "cer")){ //crt
            String content = new String(sourceFile.getBytes(), StandardCharsets.UTF_8);
            X509Certificate certificate;
            if(!content.startsWith("-----BEGIN CERTIFICATE-----")){
                String certHeader = "-----BEGIN CERTIFICATE-----\r\n";
                String certFooter = "\r\n-----END CERTIFICATE-----";
                String LINE_SEPARATOR = System.lineSeparator();
                Base64.Encoder encoder = Base64.getMimeEncoder(64, LINE_SEPARATOR.getBytes());
                byte[] certBytes = sourceFile.getBytes();
                String cert = certHeader + LINE_SEPARATOR + new String(certBytes) + LINE_SEPARATOR + certFooter;
                InputStream stream = IOUtils.toInputStream(cert);
                certificate = CertUtil.decodeCertificate(stream.readAllBytes());
            } else {
                certificate = CertUtil.decodeCertificate(sourceFile.getInputStream().readAllBytes());
            }
            publicKeyUserCertificate.setLogin(login);
            publicKeyUserCertificate.setDeleted(false);
            publicKeyUserCertificate.setSerialNumber(certificate.getSerialNumber().toString());
            publicKeyUserCertificate.setDateStart(convertRFC1123ToIsoInstant(certificate.getNotBefore().toString()));
            publicKeyUserCertificate.setDateEnd(convertRFC1123ToIsoInstant(certificate.getNotAfter().toString()));
            //publicKeyUserCertificate.setType(CONTROLLER_CA_TYPE_INPUT_AUTO);
            String surname = "";
            String givenname = "";
            var substrSubject = certificate.getSubjectDN().getName().split(", ");
            for(String s : substrSubject) {
                if(s.length()>2){
                    if(s.startsWith("SURNAME=")){
                        surname = s.substring(8);
                    }
                    if(s.startsWith("GIVENNAME=")){
                        givenname = s.substring(10);
                    }
                    if(s.startsWith("OID.1.2.643.3.131.1.1=")){
                        publicKeyUserCertificate.setInn(s.substring(22));
                    }
                }
            }
            if(!surname.isEmpty() && !givenname.isEmpty()){
                publicKeyUserCertificate.setFio(surname + ' ' + givenname);
            }

//            publicKeyUserCertificate.setDateStartPrivateKey();
//            publicKeyUserCertificate.setDateEndPrivateKey();
            publicKeyUserCertificate.setCertificate(new String(sourceFile.getBytes()));
            publicKeyUserCertificate.setImprint(DigestUtils.sha1Hex(certificate.getEncoded()));
            publicKeyUserCertificate.setKeyId(CertUtil.subjectKeyId(certificate).replace(":", ""));
            var substr = certificate.getIssuerDN().getName().split(", ");
            for(String s : substr) {
                if(s.length()>2){
                    var st = s.substring(0,3);
                    if (st.equals("CN="))
                    {
                        publicKeyUserCertificate.setIssuedWho(s.substring(3));
                        break;
                    }
                }
            }
            var existsCert = service.countPublicKeyUserCertificateByImprint(publicKeyUserCertificate.getImprint());
            if(existsCert > 0){
                throw new BadRequestException(PUBLIC_KEY_USER_CERTIFICATE_UPLOAD_EXISTS);
            }

        } else if(Objects.equals(extension, "der")) {
            //Бинарный формат, который содержит сертификат в двоичном виде
//            InputStream in = getResourceAsStream("/certyficate.cer");
//            BufferedReader br = new BufferedReader(new InputStreamReader(in));
//            try {
//                CertificateFactory cf = CertificateFactory.getInstance("X.509");
//                X509Certificate cert = (X509Certificate) cf.generateCertificate(in);
//            } catch (CertificateException e) {
//                // handle failure to decode certificate
//            }
        } else if(Objects.equals(extension, "pem")) {
            //Текстовый формат, который включает сертификат в виде закодированной в Base64 строки
        } else {
            throw new BadRequestException(PUBLIC_KEY_USER_CERTIFICATE_WRONG_FORMAT);
        }

//        PublicKeyUserCertificate publicKeyUserCertificate = new PublicKeyUserCertificate();
//        publicKeyUserCertificate.setLogin(login);
//        publicKeyUserCertificate.setDeleted(false);
        if(publicKeyUserCertificate == null){
            throw new BadRequestException(PUBLIC_KEY_USER_CERTIFICATE_IS_NULL);
        }
        PublicKeyUserCertificate response;
        try {
            response = service.savePublicKeyUserCertificate(publicKeyUserCertificate);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PUBLIC_KEY_USER_CERTIFICATE_IS_NULL);
        }
        return new ResponseEntity<>(convertToDto(response), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_PUBLIC_KEY_USER_CERTIFICATE_FIND_BY_ID_SUMMARY, description = CONTROLLER_PUBLIC_KEY_USER_CERTIFICATE_FIND_BY_ID_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> findByPublicKeyUserCertificateId(@PathVariable @Parameter(name = "id",
            description = PARAMETER_PUBLIC_KEY_USER_CERTIFICATE_ID_DESCRIPTION, example = "1" ) Long id) {
        PublicKeyUserCertificate response;
        try {
            response = service.findByPublicKeyUserCertificateId(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PUBLIC_KEY_USER_CERTIFICATE_IS_NULL);
        }
        return new ResponseEntity<>(convertToDto(response), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_PUBLIC_KEY_USER_CERTIFICATE_UPDATE_SUMMARY, description = CONTROLLER_PUBLIC_KEY_USER_CERTIFICATE_UPDATE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PutMapping("update")
    public ResponseEntity<?> updatePublicKeyUserCertificate(@RequestBody PublicKeyUserCertificateDto publicKeyUserCertificateDto) {
        PublicKeyUserCertificate publicKeyUserCertificate = convertToEntity(publicKeyUserCertificateDto);
        PublicKeyUserCertificate response;
        try {
            response = service.updatePublicKeyUserCertificate(publicKeyUserCertificate);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PUBLIC_KEY_USER_CERTIFICATE_IS_NULL);
        }
        return new ResponseEntity<>(convertToDto(response), HttpStatus.OK);
    }


    @Operation(summary = CONTROLLER_PUBLIC_KEY_USER_CERTIFICATE_DELETE_SUMMARY, description = CONTROLLER_PUBLIC_KEY_USER_CERTIFICATE_DELETE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deletePublicKeyUserCertificate(@PathVariable @Parameter(name = "id",
            description = PARAMETER_PUBLIC_KEY_USER_CERTIFICATE_ID_DESCRIPTION, example = "1" ) Long id) {
        PublicKeyUserCertificate publicKeyUserCertificate = service.findByPublicKeyUserCertificateId(id);
        publicKeyUserCertificate.setDeleted(true);
        try {
            service.updatePublicKeyUserCertificate(publicKeyUserCertificate);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PUBLIC_KEY_USER_CERTIFICATE_IS_NULL);
        }
        Map<String, String> body = new HashMap<>();
        body.put("result", "true");

        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    private PublicKeyUserCertificateDto convertToDto(PublicKeyUserCertificate publicKeyUserCertificate){
        PublicKeyUserCertificateDto publicKeyUserCertificateDto = modelMapper.map(publicKeyUserCertificate, PublicKeyUserCertificateDto.class);
        return publicKeyUserCertificateDto;
    }

    private PublicKeyUserCertificate convertToEntity(PublicKeyUserCertificateDto publicKeyUserCertificateDto){
        PublicKeyUserCertificate publicKeyUserCertificate = modelMapper.map(publicKeyUserCertificateDto, PublicKeyUserCertificate.class);
        return publicKeyUserCertificate;
    }

    private static String convertRFC1123ToIsoInstant(String date){
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
        Instant instant1 = formatter.parse(date, Instant::from);
        return instant1.toString();
    }

    public static X509Certificate readCertificate(final Resource resource) {
        try (final InputStream in = resource.getInputStream()) {
            return CertUtil.readCertificate(in);
        } catch (final Exception e) {
            throw new RuntimeException("Error reading certificate " + resource, e);
        }
    }
}
