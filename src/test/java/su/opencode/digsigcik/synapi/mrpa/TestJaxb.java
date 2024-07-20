package su.opencode.digsigcik.synapi.mrpa;

import org.junit.jupiter.api.Test;
import su.opencode.digsigcik.synapi.dto.m4d.MrpaRegistryFullXML;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static su.opencode.digsigcik.synapi.util.XMLUtil.marshall;

public class TestJaxb {
    @Test
    void name() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(MrpaRegistryFullXML.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        // Unmarshal the XML data from the file
        MrpaRegistryFullXML mrpaRegistryFullXML = (MrpaRegistryFullXML) unmarshaller.unmarshal(new File("data.xml"));
        String s = marshall(mrpaRegistryFullXML, MrpaRegistryFullXML.class);
        assertNotNull(s);
    }


}
