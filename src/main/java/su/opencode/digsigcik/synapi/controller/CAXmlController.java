package su.opencode.digsigcik.synapi.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
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
@RequestMapping("/api/v1/ca-xml")
@AllArgsConstructor
public class CAXmlController {

    private final CertificateDataService certificateDataService;

    @PostMapping(value = "/upload", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public List<String> upload(@RequestPart("srcFile") MultipartFile sourceFile) throws IOException, JAXBException {
        String content = new String(sourceFile.getBytes());
        Document document = convertStringToDocument(content);
        assert document != null;
        CAXml caXml = XMLUtil.unmarshal(document, CAXml.class);

        List<CertificateData> result2 = (caXml.getCa().stream().map(x->x.getShSystems().getShSystem().stream().map(y->y.getKeysAuthPersons().getKeyAuthPerson()
                .stream().map(z->z.getCertificates().getCertificateData()).collect(Collectors.toList())).collect(Collectors.toList())).collect(Collectors.toList())).stream()
                .flatMap(Collection::stream).flatMap(Collection::stream).flatMap(Collection::stream).collect(Collectors.toList());

        System.out.println(result2);

        for(CertificateData cert : result2){
            certificateDataService.saveCertificateData(cert);
        }
        return null;
    }

}
