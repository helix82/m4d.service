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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static su.opencode.digsigcik.synapi.util.Constant.ID_IS_NULL;
import static su.opencode.digsigcik.synapi.util.Constant.NULL;
import static su.opencode.digsigcik.synapi.util.XMLUtil.convertStringToDocument;

@RestController
@MchdExceptionHandler
@RequestMapping("/api/v1/certificate-data")
@AllArgsConstructor
public class CertificateDataController {
    private final String CA_IS_NULL = String.format("CA is %s!", NULL);
    private final CertificateDataService certificateDataService;

    @Operation(summary = "Загрузить аккредитованные УЦ из XML-файла", description = "Возвращает статус выполнения загрузки")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно загружено"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @PostMapping(value = "/upload", produces = "application/json; charset=UTF-8") //MediaType.APPLICATION_OCTET_STREAM_VALUE
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
            var stat = ca.getAccreditationStatus().getStatus();
            if(stat.equals("Действует")){
                List<CertificateData> certData = ca.getShSystems().getShSystem().stream().map(y->y.getKeysAuthPersons().getKeyAuthPerson()
                        .stream().map(z->z.getCertificates().getCertificateData()).collect(Collectors.toList())).collect(Collectors.toList()).stream().flatMap(Collection::stream).flatMap(Collection::stream).collect(Collectors.toList());
                for(CertificateData cert : certData){
//cert.getDateStart()
//        cert.getDateEnd()
                    var substr = cert.getIssuedTo().split(", ");
                    for(String s : substr) {
                        var st = s.substring(0,3);
                        if (st.equals("CN="))
                            cert.setIssuedTo(s.substring(3));
                    }
                    cert.setTypeInput("Автоматически");
                    cert.setAccreditationStatus(stat);
                    try {
                        certificateDataService.saveCertificateData(cert);
                        ca_import++;
                    } catch (ConstraintViolationException e) {
                        throw new BadRequestException(CA_IS_NULL);
                    }
                }
            }

        }

        Map<String, String> body = new HashMap<>();
        body.put("imported", Integer.toString(ca_import));

        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @Operation(summary = "Получить список аккредитованных УЦ", description = "Возвращает список аккредитованных УЦ")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено"),
            @ApiResponse(code = 404, message = "Not found - Ни одна запись не найдена")
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

    @Operation(summary = "Сохранить новую запись аккредитованных УЦ", description = "Возвращает сохраненную запись аккредитованной УЦ")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно сохранено"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @PostMapping(value = "save", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> saveCertificateData(@RequestBody CertificateData certificateData) {
        CertificateData certificateDataSave;
        certificateData.setTypeInput("Вручную");
        try {
            certificateDataSave = certificateDataService.saveCertificateData(certificateData);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(CA_IS_NULL);
        }

        return new ResponseEntity<>(certificateDataSave, HttpStatus.OK);
    }

    @Operation(summary = "Получить запись аккредитованной УЦ по ИД", description = "Возвращает запись аккредитованной УЦ")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено"),
            @ApiResponse(code = 404, message = "Not found - Ни одна запись не найдена")
    })
    @GetMapping(value = "/{id}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> findByCertificateDataId(@PathVariable Long id) {
        CertificateData certificateDataFind;
        try {
            certificateDataFind = certificateDataService.findByCertificateDataId(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(CA_IS_NULL);
        }
        return new ResponseEntity<>(certificateDataFind, HttpStatus.OK);
    }

    @Operation(summary = "Сохранить изменения аккредитованной УЦ", description = "Возвращает запись аккредитованной УЦ")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно изменено"),
            @ApiResponse(code = 404, message = "Not found")
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

    @Operation(summary = "Удалить запись аккредитованной УЦ по ИД", description = "Ничего не возвращает")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно удалено"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @DeleteMapping(value = "delete/{id}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> deleteCertificateData(@PathVariable @Parameter(name = "id",
            description = "ИД CA", example = "1" ) Long id) {
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
