package su.opencode.digsigcik.m4d.mrpa;

import org.junit.jupiter.api.Test;
import su.opencode.digsigcik.m4d.dto.M4dFullXML;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static su.opencode.digsigcik.m4d.util.XMLUtil.marshall;

public class TestJaxb {
    @Test
    void name() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(M4dFullXML.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        // Unmarshal the XML data from the file
        M4dFullXML m4dFullXML = (M4dFullXML) unmarshaller.unmarshal(new File("data.xml"));
        String s = marshall(m4dFullXML, M4dFullXML.class);
        assertNotNull(s);
    }


}
