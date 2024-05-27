package su.opencode.digsigcik.synapi.ca;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.w3c.dom.Document;
import su.opencode.digsigcik.synapi.model.ca.CAXml;
import su.opencode.digsigcik.synapi.util.XMLUtil;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
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
        assertNotNull(caXml);

    }

    @Test
    public void givenStringData_whenUsingJavaMap_thenSplitAndValidate() {
        String data = "CN=УЦ Ключ-Л, OU=Удостоверяющий Центр, O=ООО Ключ-Л, L=Челябинск, S=Челябинская область, C=RU, E=key@key74.ru, ИНН=007451261819, ОГРН=1087451001560";
        Map<String, String> keyValueMap = Arrays.stream(data.split(", "))
                .map(kv -> kv.split("="))
                .filter(kvArray -> kvArray.length == 2)
                .collect(Collectors.toMap(kv -> kv[0], kv -> kv[1]));

        System.out.println(keyValueMap.get("CN"));
        assertNotNull(keyValueMap.get("CN"));
    }
}
