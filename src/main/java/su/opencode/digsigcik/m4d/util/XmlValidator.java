package su.opencode.digsigcik.m4d.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

public class XmlValidator {
    private static final Logger LOGGER = LogManager.getLogger(XmlValidator.class);

    private final String xsdPath;
    private final InputStream xmlString;

    public XmlValidator(String xsdPath, InputStream xmlString) {
        this.xsdPath = xsdPath;
        this.xmlString = xmlString;
    }

    public boolean isValid() throws IOException, SAXException {
        Validator validator = initValidator(xsdPath);
        try {
            validator.validate(new StreamSource(xmlString));
            return true;
        } catch (SAXException e) {
            return false;
        }
    }

    public List<SAXParseException> listParsingExceptions() throws IOException, SAXException {
        XmlErrorHandler xsdErrorHandler = new XmlErrorHandler();
        Validator validator = initValidator(xsdPath);
        validator.setErrorHandler(xsdErrorHandler);
        try {
            validator.validate(new StreamSource(xmlString));
        } catch (SAXParseException ignored) {}
        xsdErrorHandler.getExceptions().forEach(e -> LOGGER.info(String.format("Line number: %s, Column number: %s. %s", e.getLineNumber(), e.getColumnNumber(), e.getMessage())));
        return xsdErrorHandler.getExceptions();
    }

    private Validator initValidator(String xsdPath) throws SAXException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source schemaFile = new StreamSource(xsdPath);
        Schema schema = factory.newSchema(schemaFile);
        return schema.newValidator();
    }

    private File getFile(String location) {
        return new File(Objects.requireNonNull(getClass().getClassLoader().getResource(location)).getFile());
    }
}
