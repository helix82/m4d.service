package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import su.opencode.digsigcik.synapi.model.ca.CAXml;
import su.opencode.digsigcik.synapi.model.ca.CertificateData;
import su.opencode.digsigcik.synapi.service.CertificateDataService;
import su.opencode.digsigcik.synapi.util.XMLUtil;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static su.opencode.digsigcik.synapi.util.XMLUtil.convertStringToDocument;

@RestController
@RequestMapping("/api/v1/certificate-data")
@AllArgsConstructor
public class CertificateDataController {

    private final CertificateDataService certificateDataService;

    @Operation(summary = "Загрузить аккредитованные УЦ из XML-файла", description = "Возвращает статус выполнения загрузки")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно загружено"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @PostMapping(value = "/upload", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public String upload(@RequestPart("srcFile") MultipartFile sourceFile) throws IOException, JAXBException {
        String content = new String(sourceFile.getBytes());
        Document document = convertStringToDocument(content);
        assert document != null;
        CAXml caXml = XMLUtil.unmarshal(document, CAXml.class);

        for(CAXml.CA ca : caXml.getCa()){
            var stat = ca.getAccreditationStatus().getStatus();
            List<CertificateData> certData = ca.getShSystems().getShSystem().stream().map(y->y.getKeysAuthPersons().getKeyAuthPerson()
                    .stream().map(z->z.getCertificates().getCertificateData()).collect(Collectors.toList())).collect(Collectors.toList()).stream().flatMap(Collection::stream).flatMap(Collection::stream).collect(Collectors.toList());
            for(CertificateData cert : certData){

                var substr = cert.getIssuedTo().split(", ");
                for(String s : substr) {
                    var st = s.substring(0,3);
                    if (st.equals("CN="))
                        cert.setIssuedTo(s.substring(3));
                }
                cert.setTypeInput("Автоматически");
                cert.setAccreditationStatus(stat);
                certificateDataService.saveCertificateData(cert);
            }
        }
        return "OK";
    }

    @Operation(summary = "Получить список аккредитованных УЦ", description = "Возвращает список аккредитованных УЦ")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено"),
            @ApiResponse(code = 404, message = "Not found - Ни одна запись не найдена")
    })
    @GetMapping()
    public List<CertificateData> findAllCertificateData() {
        return certificateDataService.findAllCertificateData();
    }

    @Operation(summary = "Сохранить новую запись аккредитованных УЦ", description = "Возвращает сохраненную запись аккредитованной УЦ")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно сохранено"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @PostMapping("save")
    public CertificateData saveCertificateData(@RequestBody CertificateData certificateData) {
        certificateData.setTypeInput("Вручную");
        return certificateDataService.saveCertificateData(certificateData);
    }

    @Operation(summary = "Получить запись аккредитованной УЦ по ИД", description = "Возвращает запись аккредитованной УЦ")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено"),
            @ApiResponse(code = 404, message = "Not found - Ни одна запись не найдена")
    })
    @GetMapping("/{id}")
    public CertificateData findByCertificateDataId(@PathVariable Long id) {
        return certificateDataService.findByCertificateDataId(id);
    }

    @Operation(summary = "Сохранить изменения аккредитованной УЦ", description = "Возвращает запись аккредитованной УЦ")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно изменено"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @PutMapping("update")
    public CertificateData updateCertificateData(@RequestBody CertificateData certificateData) {
        return certificateDataService.updateCertificateData(certificateData);
    }

    @Operation(summary = "Удалить запись аккредитованной УЦ по ИД", description = "Ничего не возвращает")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно удалено"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @DeleteMapping("delete/{id}")
    public void deleteCertificateData(@PathVariable Long id) {
        certificateDataService.deleteCertificateData(id);
    }

}
