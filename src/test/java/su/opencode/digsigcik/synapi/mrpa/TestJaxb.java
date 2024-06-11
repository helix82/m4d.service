package su.opencode.digsigcik.synapi.mrpa;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class TestJaxb {
    @Test
    void testMarshalToFile() throws JAXBException, IOException {
        String value = "Hello, world!";
        JAXBContext jaxbContext = JAXBContext.newInstance(Object.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(value, System.out);


        assertNotNull(value);

//        String content = Files.readString(Path.of("./tsl1.xml"), StandardCharsets.UTF_8);
//
//        Document document = convertStringToDocument(content);
//        CAXml caXml = XMLUtil.unmarshal(document, CAXml.class);
//
//
//        List<String> result = (caXml.getCa().stream().map(x -> x.getShSystems().getShSystem().stream().map(y -> y.getKeysAuthPersons().getKeyAuthPerson()
//                .stream().map(z -> z.getCertificates().getCertificateData().stream().map(CertificateData::getIssuedTo).collect(Collectors.toList())).collect(Collectors.toList())).collect(Collectors.toList())).collect(Collectors.toList())).stream()
//                .flatMap(Collection::stream).flatMap(Collection::stream).flatMap(Collection::stream).collect(Collectors.toList());
//
//        for(String str : result){
//            var substr = str.split(", ");
//            for(String s : substr) {
//                var st = s.substring(0,3);
//                if (st.equals("CN="))
//                    System.out.println(s.substring(3));
//            }
//
//        }
//
//        assertNotNull(caXml);

    }

}
