package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.PrivateKeyUsagePeriod;
import org.cryptacular.util.CertUtil;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.annotation.MchdExceptionHandler;
import su.opencode.digsigcik.synapi.dto.PublicKeyUserCertificateDto;
import su.opencode.digsigcik.synapi.enums.StatusStatementEnum;
import su.opencode.digsigcik.synapi.enums.TypeUserCertificateEnum;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.model.PublicKeyStatement;
import su.opencode.digsigcik.synapi.model.PublicKeyUserCertificate;
import su.opencode.digsigcik.synapi.model.ca.CertificateData;
import su.opencode.digsigcik.synapi.service.CertificateDataService;
import su.opencode.digsigcik.synapi.service.PublicKeyStatementService;
import su.opencode.digsigcik.synapi.service.PublicKeyUserCertificateService;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateEncodingException;
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
    private final PublicKeyStatementService statementService;
    private final CertificateDataService certificateDataService;
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

    @Operation(summary = CONTROLLER_PUBLIC_KEY_USER_CERTIFICATE_PARSE_SUMMARY, description = CONTROLLER_PUBLIC_KEY_USER_CERTIFICATE_PARSE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PostMapping(value = "parse", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> parsePublicKeyUserCertificate(@RequestPart("srcFile") MultipartFile sourceFile) throws IOException, CertificateException{
        String extension = FilenameUtils.getExtension(sourceFile.getOriginalFilename());
        PublicKeyUserCertificate publicKeyUserCertificate;
        if(Objects.equals(extension, "cer")){ //crt
            publicKeyUserCertificate = parseCertificateCER(sourceFile);
        } else if(Objects.equals(extension, "der")) {
            //TODO @helix82 парсинг DER-сертификатов publicKeyUserCertificate = parseCertificateDER(sourceFile);
            throw new BadRequestException(PUBLIC_KEY_USER_CERTIFICATE_WRONG_FORMAT);
        } else if(Objects.equals(extension, "pem")) {
            //TODO @helix82 парсинг PEM-сертификатов publicKeyUserCertificate = parseCertificatePEM(sourceFile);
            throw new BadRequestException(PUBLIC_KEY_USER_CERTIFICATE_WRONG_FORMAT);
        } else {
            throw new BadRequestException(PUBLIC_KEY_USER_CERTIFICATE_WRONG_FORMAT);
        }
        if(publicKeyUserCertificate == null){
            throw new BadRequestException(PUBLIC_KEY_USER_CERTIFICATE_IS_NULL);
        }
        return new ResponseEntity<>(convertWithoutStatementToDto(publicKeyUserCertificate), HttpStatus.OK);
    }


    @Operation(summary = CONTROLLER_PUBLIC_KEY_USER_CERTIFICATE_SAVE_SUMMARY, description = CONTROLLER_PUBLIC_KEY_USER_CERTIFICATE_SAVE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PostMapping(value = "save", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> savePublicKeyUserCertificate(@RequestBody PublicKeyUserCertificateDto publicKeyUserCertificateDto) {
        PublicKeyUserCertificate publicKeyUserCertificate = convertToEntity(publicKeyUserCertificateDto);
        var existsCert = service.countPublicKeyUserCertificateByImprint(publicKeyUserCertificate.getImprint());
        if(existsCert > 0){
            throw new BadRequestException(PUBLIC_KEY_USER_CERTIFICATE_UPLOAD_EXISTS);
        }
        PublicKeyUserCertificate response;
        try {
            response = service.savePublicKeyUserCertificate(publicKeyUserCertificate);
            if(publicKeyUserCertificate.getStatementId() != null) {
                PublicKeyStatement pkStatement;
                try {
                    pkStatement = statementService.findByPublicKeyStatementId(response.getStatementId());
                } catch (ConstraintViolationException e) {
                    throw new BadRequestException(PUBLIC_KEY_STATEMENT_IS_NULL);
                }
                pkStatement.setStatus(StatusStatementEnum.PROCESSED);
                try {
                    statementService.updatePublicKeyStatement(pkStatement);
                } catch (ConstraintViolationException e) {
                    throw new BadRequestException(PUBLIC_KEY_STATEMENT_IS_NULL);
                }
            }
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
        if(publicKeyUserCertificate.getStatementId() != null){
            PublicKeyStatement publicKeyStatement = statementService.findByPublicKeyStatementId(publicKeyUserCertificate.getStatementId());
            publicKeyUserCertificateDto.setLogin(publicKeyStatement.getLogin());
        }
        if(publicKeyUserCertificateDto.getLogin() == null){
            throw new BadRequestException(PUBLIC_KEY_USER_CERTIFICATE_STATEMENT_ID_AND_LOGIN_NOT_FOUND);
        }
        return publicKeyUserCertificateDto;
    }

    private PublicKeyUserCertificateDto convertWithoutStatementToDto(PublicKeyUserCertificate publicKeyUserCertificate){
        PublicKeyUserCertificateDto publicKeyUserCertificateDto = modelMapper.map(publicKeyUserCertificate, PublicKeyUserCertificateDto.class);
        return publicKeyUserCertificateDto;
    }

    private PublicKeyUserCertificate convertToEntity(PublicKeyUserCertificateDto publicKeyUserCertificateDto){
        PublicKeyUserCertificate publicKeyUserCertificate = modelMapper.map(publicKeyUserCertificateDto, PublicKeyUserCertificate.class);
        if(publicKeyUserCertificateDto.getStatementId() != null){
            PublicKeyStatement publicKeyStatement = statementService.findByPublicKeyStatementId(publicKeyUserCertificateDto.getStatementId());
            publicKeyUserCertificate.setLogin(publicKeyStatement.getLogin());
        }
        if(publicKeyUserCertificate.getLogin() == null){
            throw new BadRequestException(PUBLIC_KEY_USER_CERTIFICATE_STATEMENT_ID_AND_LOGIN_NOT_FOUND);
        }
        return publicKeyUserCertificate;
    }

    private static String convertRFC1123ToIsoInstant(String date){
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
        Instant instant1 = formatter.parse(date, Instant::from);
        return instant1.toString();
    }

    private static String convertDateCertificateToIsoInstant(String date){
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyyMMddHHmmssz", Locale.ENGLISH);
        Instant instant1 = formatter.parse(date, Instant::from);
        return instant1.toString();
    }

    public PublicKeyUserCertificate parseCertificateCER(MultipartFile sourceFile) throws IOException, CertificateEncodingException {
        PublicKeyUserCertificate publicKeyUserCertificate = new PublicKeyUserCertificate();
        String content = new String(sourceFile.getBytes(), StandardCharsets.UTF_8);
        X509Certificate certificate;
        if(!content.startsWith("-----BEGIN CERTIFICATE-----")){
            String certHeader = "-----BEGIN CERTIFICATE-----\r\n";
            String certFooter = "\r\n-----END CERTIFICATE-----";
            String LINE_SEPARATOR = System.lineSeparator();
            byte[] certBytes = sourceFile.getBytes();
            String cert = certHeader + LINE_SEPARATOR + new String(certBytes) + LINE_SEPARATOR + certFooter;
            InputStream stream = IOUtils.toInputStream(cert);
            certificate = CertUtil.decodeCertificate(stream.readAllBytes());
        } else {
            certificate = CertUtil.decodeCertificate(sourceFile.getInputStream().readAllBytes());
        }
        publicKeyUserCertificate.setDeleted(false);
        publicKeyUserCertificate.setSerialNumber(certificate.getSerialNumber().toString(16));
        publicKeyUserCertificate.setDateStart(convertRFC1123ToIsoInstant(certificate.getNotBefore().toString()));
        publicKeyUserCertificate.setDateEnd(convertRFC1123ToIsoInstant(certificate.getNotAfter().toString()));
        var substr1 = certificate.getIssuerDN().getName().split(", ");
        String ogrnIssuer, innIssuer, streetIssuer, cIssuer, sIssuer, lIssuer, oIssuer, cnIssuer;
        ogrnIssuer = innIssuer = streetIssuer = cIssuer = sIssuer = lIssuer = oIssuer = cnIssuer = "";
        for(String s : substr1) {
            if(s.length()>2){
                if (s.startsWith("CN=")) {
                    cnIssuer = s.substring(3);
                }
                if (s.startsWith("O=")) {
                    oIssuer = s.substring(2);
                }
                if (s.startsWith("STREET=")) {
                    streetIssuer = s.substring(7);
                }
                if (s.startsWith("L=")) {
                    lIssuer = s.substring(2);
                }
                if (s.startsWith("ST=")) {
                    sIssuer = s.substring(3);
                }
                if (s.startsWith("C=")) {
                    cIssuer = s.substring(2);
                }
                if(s.startsWith("OID.1.2.643.100.1=")){
                    String hexStr = s.substring(23);
                    StringBuilder output = new StringBuilder();
                    for (int i = 0; i < hexStr.length(); i += 2) {
                        String str1 = hexStr.substring(i, i + 2);
                        output.append((char) Integer.parseInt(str1, 16));
                    }
                    ogrnIssuer = output.toString();
                }
                if(s.startsWith("OID.1.2.643.100.4=")){
                    String hexStr = s.substring(23);
                    StringBuilder output = new StringBuilder();
                    for (int i = 0; i < hexStr.length(); i += 2) {
                        String str1 = hexStr.substring(i, i + 2);
                        output.append((char) Integer.parseInt(str1, 16));
                    }
                    innIssuer = output.toString();
                }
            }
        }
        publicKeyUserCertificate.setIssuerWho("ОГРН=" + ogrnIssuer + ", ИНН=" + innIssuer + ", STREET=" + streetIssuer + ", C=" + cIssuer + ", S=" + sIssuer + ", L=" + lIssuer + ", O=" + oIssuer + ", CN=" + cnIssuer);

        String oid1 = Extension.authorityKeyIdentifier.getId();
        byte[] extValue1 = certificate.getExtensionValue(oid1);
        if (extValue1 == null) {
            throw new BadRequestException(PUBLIC_KEY_USER_CERTIFICATE_IS_NOT);
        }
        ASN1OctetString akiOc1 = ASN1OctetString.getInstance(extValue1);
        AuthorityKeyIdentifier aki1 = AuthorityKeyIdentifier.getInstance(akiOc1.getOctets());
        String keyIdIssuer = new DEROctetString(aki1.getKeyIdentifier()).toString().substring(1);

        TypeUserCertificateEnum typeCert = TypeUserCertificateEnum.NOT_QUALIFIED;
        CertificateData certificateData;
        try {
            certificateData = certificateDataService.findCertificateDataByKeyId(keyIdIssuer);
            if(certificateData != null){
                var certIssuer = certificateData.getIssuedTo().replace("\\","").replace("\"", "").toLowerCase();
                var cnIssuerLC = cnIssuer.replace("\\","").replace("\"", "").toLowerCase();
                if(certIssuer.equals(cnIssuerLC)){
                    typeCert = TypeUserCertificateEnum.QUALIFIED;
                }
            }
        } catch (ConstraintViolationException e) {
            typeCert = TypeUserCertificateEnum.NOT_QUALIFIED;
        }
        publicKeyUserCertificate.setType(typeCert);

        String surname = "";
        String givenname = "";
        String fio = "";
        String inn = "";
        var substrSubject = certificate.getSubjectDN().getName().split(", ");
        for(String s : substrSubject) {
            if(s.length()>2){
                if(s.startsWith("O=")){
                    publicKeyUserCertificate.setOrganization(s.substring(2));
                }
                if(s.startsWith("CN=")){
                    fio = s.substring(3);
                }
                if(s.startsWith("SURNAME=")){
                    surname = s.substring(8);
                }
                if(s.startsWith("GIVENNAME=")){
                    givenname = s.substring(10);
                }
                if(s.startsWith("OID.1.2.643.3.131.1.1=")){
                    String hexStr = s.substring(27);
                    StringBuilder output = new StringBuilder();
                    for (int i = 0; i < hexStr.length(); i += 2) {
                        String str1 = hexStr.substring(i, i + 2);
                        output.append((char) Integer.parseInt(str1, 16));
                    }
                    inn = output.toString();
                }
            }
        }
        if(inn != ""){
            publicKeyUserCertificate.setInn(inn);
        } else {
            publicKeyUserCertificate.setInn("632132430190");
        }
        if(surname != "" && givenname != ""){
            publicKeyUserCertificate.setFio(surname + ' ' + givenname);
        } else if(fio != "") {
            publicKeyUserCertificate.setFio(fio);
        }
        String dateStartPrivateKey = "";
        String dateEndPrivateKey = "";
        String oid = Extension.privateKeyUsagePeriod.getId();
        byte[] extValue = certificate.getExtensionValue(oid);
        if(extValue != null) {
            ASN1OctetString akiOc = ASN1OctetString.getInstance(extValue);
            PrivateKeyUsagePeriod aki = PrivateKeyUsagePeriod.getInstance(akiOc.getOctets());
            dateStartPrivateKey = convertDateCertificateToIsoInstant(aki.getNotBefore().getTime());
            dateEndPrivateKey = convertDateCertificateToIsoInstant(aki.getNotAfter().getTime());
        } else {
            dateStartPrivateKey = convertRFC1123ToIsoInstant(certificate.getNotBefore().toString());
            dateEndPrivateKey = convertRFC1123ToIsoInstant(certificate.getNotAfter().toString());
        }
        publicKeyUserCertificate.setDateStartPrivateKey(dateStartPrivateKey); //2.5.29.16
        publicKeyUserCertificate.setDateEndPrivateKey(dateEndPrivateKey);

        publicKeyUserCertificate.setCertificate(new String(sourceFile.getBytes()));
        publicKeyUserCertificate.setImprint(DigestUtils.sha1Hex(certificate.getEncoded()));
        publicKeyUserCertificate.setKeyId(CertUtil.subjectKeyId(certificate).replace(":", ""));

        return publicKeyUserCertificate;
    }

    public PublicKeyUserCertificate parseCertificateDER(MultipartFile sourceFile) {
        PublicKeyUserCertificate publicKeyUserCertificate = new PublicKeyUserCertificate();
        //Бинарный формат, который содержит сертификат в двоичном виде
        return publicKeyUserCertificate;
    }

    public PublicKeyUserCertificate parseCertificatePEM(MultipartFile sourceFile) {
        PublicKeyUserCertificate publicKeyUserCertificate = new PublicKeyUserCertificate();
        //Текстовый формат, который включает сертификат в виде закодированной в Base64 строки
        return publicKeyUserCertificate;
    }
}
