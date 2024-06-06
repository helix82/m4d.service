package su.opencode.digsigcik.synapi.ca;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.w3c.dom.Document;
import su.opencode.digsigcik.synapi.model.ca.CAXml;
import su.opencode.digsigcik.synapi.model.ca.CertificateData;
import su.opencode.digsigcik.synapi.util.XMLUtil;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static su.opencode.digsigcik.synapi.util.XMLUtil.convertStringToDocument;


@SpringBootTest
public class TestJaxb {

    @Test
    void testUnMarshalFromFileCA() throws JAXBException, IOException {
        String content = Files.readString(Path.of("./tsl1.xml"), StandardCharsets.UTF_8);

        Document document = convertStringToDocument(content);
        CAXml caXml = XMLUtil.unmarshal(document, CAXml.class);


        List<String> result = (caXml.getCa().stream().map(x -> x.getShSystems().getShSystem().stream().map(y -> y.getKeysAuthPersons().getKeyAuthPerson()
                .stream().map(z -> z.getCertificates().getCertificateData().stream().map(CertificateData::getIssuedTo).collect(Collectors.toList())).collect(Collectors.toList())).collect(Collectors.toList())).collect(Collectors.toList())).stream()
                .flatMap(Collection::stream).flatMap(Collection::stream).flatMap(Collection::stream).collect(Collectors.toList());

        for(String str : result){
            var substr = str.split(", ");
            for(String s : substr) {
                var st = s.substring(0,3);
               if (st.equals("CN="))
                   System.out.println(s.substring(3));
            }

        }

        assertNotNull(caXml);

    }

}
