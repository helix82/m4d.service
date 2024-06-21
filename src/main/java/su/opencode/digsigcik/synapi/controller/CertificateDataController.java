package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import su.opencode.digsigcik.synapi.annotation.MchdExceptionHandler;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.model.ca.CAXml;
import su.opencode.digsigcik.synapi.model.ca.CertificateData;
import su.opencode.digsigcik.synapi.service.CertificateDataService;
import su.opencode.digsigcik.synapi.util.XMLUtil;

import javax.validation.ConstraintViolationException;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static su.opencode.digsigcik.synapi.util.Constant.ID_IS_NULL;
import static su.opencode.digsigcik.synapi.util.Constants.*;
import static su.opencode.digsigcik.synapi.util.XMLUtil.convertStringToDocument;

@RestController
@MchdExceptionHandler
@RequestMapping("/api/v1/certificate-data")
@AllArgsConstructor
public class CertificateDataController {
    private final CertificateDataService certificateDataService;

    @Operation(summary = CONTROLLER_CA_UPLOAD_SUMMARY, description = CONTROLLER_CA_UPLOAD_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PostMapping(value = "/upload", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> upload(@RequestPart("srcFile") MultipartFile sourceFile) throws IOException, JAXBException {
        String content = new String(sourceFile.getBytes());
        Document document = convertStringToDocument(content);
        assert document != null;
        CAXml caXml = XMLUtil.unmarshal(document, CAXml.class);

        if(caXml == null) {
            throw new BadRequestException(CA_IS_NULL);
        }
        int ca_import = 0;
        for(CAXml.CA ca : caXml.getCa()){

            String stat = ca.getAccreditationStatusCode(ca.getAccreditationStatus().getStatus());
            if(Objects.equals(stat, "VALID")){
                if(ca.getShSystems().getShSystem() != null){
                    System.out.println(ca.getShSystems().getShSystem().stream().map(y->y.getKeysAuthPersons().getKeyAuthPerson()).collect(Collectors.toList()).stream().flatMap(Collection::stream).collect(Collectors.toList()));
                    List<CAXml.CA.SHSystems.SHSystem.KeysAuthPersons.KeyAuthPerson> keyAuthPerson = ca.getShSystems().getShSystem().stream().map(y->y.getKeysAuthPersons().getKeyAuthPerson()).collect(Collectors.toList()).stream().flatMap(Collection::stream).collect(Collectors.toList());
                    for(CAXml.CA.SHSystems.SHSystem.KeysAuthPersons.KeyAuthPerson keyAP: keyAuthPerson){
                        var keyId = keyAP.getKeyId();
                        List<CertificateData> certData = keyAP.getCertificates().getCertificateData();
                        for(CertificateData cert : certData){
                            var dateStart = LocalDateTime.parse(cert.getDateStart(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
                            var dateEnd = LocalDateTime.parse(cert.getDateEnd(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
                            var currDateTime = LocalDateTime.now();
                            var existsCert = certificateDataService.countCertificateDataByImprint(cert.getImprint());
                            if(existsCert == 0){
                                if(!currDateTime.isBefore(dateStart) && !currDateTime.isAfter(dateEnd)){
                                    var substr = cert.getIssuedTo().split(", ");
                                    for(String s : substr) {
                                        if(s.length()>2){
                                            var st = s.substring(0,3);
                                            if (st.equals("CN="))
                                            {
                                                cert.setIssuedTo(s.substring(3));
                                                break;
                                            }
                                        }
                                    }
                                    cert.setTypeInput(CONTROLLER_CA_TYPE_INPUT_AUTO);
                                    cert.setAccreditationStatus(stat);
                                    cert.setKeyId(keyId);

                                    try {
                                        certificateDataService.saveCertificateData(cert);
                                        ca_import++;
                                    } catch (ConstraintViolationException e) {
                                        throw new BadRequestException(CA_IS_NULL);
                                    }
                                }
                            }
                        }
                    }
                }

            }

        }

        Map<String, String> body = new HashMap<>();
        body.put("imported", Integer.toString(ca_import));

        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_CA_FIND_ALL_SUMMARY, description = CONTROLLER_CA_FIND_ALL_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping(produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> findAllCertificateData() {
        List<CertificateData> CAFindByInn;
        try {
            CAFindByInn = certificateDataService.findAllCertificateData();
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(CA_IS_NULL);
        }
        return new ResponseEntity<>(CAFindByInn, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_CA_SAVE_SUMMARY, description = CONTROLLER_CA_SAVE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PostMapping(value = "save", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> saveCertificateData(@RequestBody CertificateData certificateData) {
        CertificateData certificateDataSave;
        certificateData.setTypeInput(CONTROLLER_CA_TYPE_INPUT_MANUALLY);
        try {
            certificateDataSave = certificateDataService.saveCertificateData(certificateData);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(CA_IS_NULL);
        }

        return new ResponseEntity<>(certificateDataSave, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_CA_FIND_BY_ID_SUMMARY, description = CONTROLLER_CA_FIND_BY_ID_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping(value = "/{id}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> findByCertificateDataId(@PathVariable @Parameter(name = "id",
            description = PARAMETER_CA_ID_DESCRIPTION, example = "1" ) Long id) {
        CertificateData certificateDataFind;
        try {
            certificateDataFind = certificateDataService.findByCertificateDataId(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(CA_IS_NULL);
        }
        return new ResponseEntity<>(certificateDataFind, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_CA_UPDATE_SUMMARY, description = CONTROLLER_CA_UPDATE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PutMapping(value = "update", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> updateCertificateData(@RequestBody CertificateData certificateData) {
        CertificateData certificateDataUpdate;
        try {
            certificateDataUpdate = certificateDataService.updateCertificateData(certificateData);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(CA_IS_NULL);
        }
        return new ResponseEntity<>(certificateDataUpdate, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_CA_DELETE_SUMMARY, description = CONTROLLER_CA_DELETE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @DeleteMapping(value = "delete/{id}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> deleteCertificateData(@PathVariable @Parameter(name = "id",
            description = PARAMETER_CA_ID_DESCRIPTION, example = "1" ) Long id) {
        try {
            certificateDataService.deleteCertificateData(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(ID_IS_NULL);
        }
        Map<String, String> body = new HashMap<>();
        body.put("result", "true");

        return new ResponseEntity<>(body, HttpStatus.OK);
    }

}
