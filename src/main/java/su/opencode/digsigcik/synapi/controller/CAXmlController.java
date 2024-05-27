package su.opencode.digsigcik.synapi.controller;

import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import su.opencode.digsigcik.synapi.model.ca.CAXml;
import su.opencode.digsigcik.synapi.util.XMLUtil;

import javax.xml.bind.JAXBException;
import java.io.IOException;

import static su.opencode.digsigcik.synapi.util.XMLUtil.convertStringToDocument;

@RestController
@RequestMapping("/api/v1/ca-xml")
@AllArgsConstructor
public class CAXmlController {

    @PostMapping(value = "/upload", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> upload(@RequestPart("srcFile") MultipartFile sourceFile) throws IOException, JAXBException {
        String content = new String(sourceFile.getBytes());
        Document document = convertStringToDocument(content);
        CAXml caXml = XMLUtil.unmarshal(document, CAXml.class);
        System.out.println(caXml);
        return null;
    }
}
