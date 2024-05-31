package su.opencode.digsigcik.synapi.controller;

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

    @GetMapping()
    public List<CertificateData> findAllCertificateData() {
        return certificateDataService.findAllCertificateData();
    }

    @PostMapping("save")
    public CertificateData saveCertificateData(@RequestBody CertificateData certificateData) {
        certificateData.setTypeInput("Вручную");
        return certificateDataService.saveCertificateData(certificateData);
    }

    @GetMapping("/{id}")
    public CertificateData findByCertificateDataId(@PathVariable Long id) {
        return certificateDataService.findByCertificateDataId(id);
    }

    @PutMapping("update")
    public CertificateData updateCertificateData(@RequestBody CertificateData certificateData) {
        return certificateDataService.updateCertificateData(certificateData);
    }

    @DeleteMapping("delete/{id}")
    public void deleteCertificateData(@PathVariable Long id) {
        certificateDataService.deleteCertificateData(id);
    }

}
