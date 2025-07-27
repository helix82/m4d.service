package su.opencode.digsigcik.m4d.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import su.opencode.digsigcik.m4d.dto.ReportValidSchemeDto;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@UtilityClass
public class XMLUtil {

    public static <T> T unmarshal(@NonNull final Document doc, Class<T> clazz) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(clazz);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Object obj = unmarshaller.unmarshal(doc);
        return clazz.cast(obj);
    }

    public static String marshall(@NonNull final Object wrapper, @NonNull final Class<?> clazz) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(clazz);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(wrapper, stringWriter);
        return stringWriter.toString();
    }

    public static Document convertStringToDocument(String xmlStr) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try
        {
            builder = factory.newDocumentBuilder();
            return builder.parse( new InputSource( new StringReader( xmlStr)));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public static String getSubstringFromXml(String content, String subStringParam){
        String subString;
        int startIndex = content.indexOf(subStringParam);
        if (startIndex != -1) {
            startIndex += subStringParam.length();
            int endIndex = content.indexOf("\"", startIndex);
            if (endIndex != -1) {
                subString = content.substring(startIndex, endIndex);
                return subString;
            } else {
                System.out.println("Закрывающая кавычка не найдена.");
                return "error";
            }
        } else {
            System.out.println("Подстрока не найдена.");
            return "not_found";
        }
    }

    public static ReportValidSchemeDto validXmlM4dScheme(MultipartFile sourceFile, String reqScheme) throws IOException, SAXException {
        String scheme;
        ReportValidSchemeDto report = new ReportValidSchemeDto();
        report.setFileName(sourceFile.getOriginalFilename());
        if (reqScheme.isEmpty()) {
            report.setReqSchema("none");
            String content = readXmlToString(sourceFile);
            scheme = XMLUtil.getSubstringFromXml(content, "ВерсФорм=\"");
        } else {
            report.setReqSchema(reqScheme);
            scheme = reqScheme;
        }
        if (!scheme.equals("error") && !scheme.equals("not_found")) {
            if (scheme.equals("EMCHD_1")) {
                report.setOutSchema("EMCHD_1 (003)");
                List<SAXParseException> res = new XmlValidator(sourceFile.getInputStream(), scheme).listParsingExceptions();
                if (!res.isEmpty()) {
                    Map<String, String> body = new HashMap<>();
                    List<String> errs = new ArrayList<>();
                    res.forEach(e -> errs.add(String.format("Line number: %s, Column number: %s. %s", e.getLineNumber(), e.getColumnNumber(), e.getMessage())));
                    body.put("details", String.join(System.lineSeparator(), errs));
                    report.setResult("false");
                    report.setErrorDetail(body.toString());
                } else {
                    report.setResult("true");
                }
            }
        }
        return report;
    }

    public String readXmlToString(MultipartFile sourceFile){
        String content;
        try (InputStream is = sourceFile.getInputStream()) {
            byte[] bom = new byte[3];
            int n = is.read(bom, 0, bom.length);
            Reader reader;
            if (n == 3 && (bom[0] & 0xFF) == 0xEF && (bom[1] & 0xFF) == 0xBB && (bom[2] & 0xFF) == 0xBF) {
                reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            } else {
                reader = new InputStreamReader(sourceFile.getInputStream(), StandardCharsets.UTF_8);
            }
            StringBuilder cont = new StringBuilder();
            char[] buffer = new char[1024];
            int numRead;
            while ((numRead = reader.read(buffer)) != -1) {
                cont.append(buffer, 0, numRead);
            }
            content = cont.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return content;
    }
}

