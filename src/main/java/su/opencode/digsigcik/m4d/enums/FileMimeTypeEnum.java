package su.opencode.digsigcik.m4d.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.io.FilenameUtils;
import org.springframework.util.StringUtils;

import java.net.FileNameMap;
import java.net.URLConnection;

public enum FileMimeTypeEnum {
    DSCP("application/octet-stream"),
    XML("text/xml"),
    PDF("application/pdf"),
// Microsoft Office
    DOC("application/msword"),
    DOT("application/msword"),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    DOTX("application/vnd.openxmlformats-officedocument.wordprocessingml.template"),
    DOCM("application/vnd.ms-word.document.macroEnabled.12"),
    DOTM("application/vnd.ms-word.template.macroEnabled.12"),
    XLS("application/vnd.ms-excel"),
    XLT("application/vnd.ms-excel"),
    XLA("application/vnd.ms-excel"),
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    XLTX("application/vnd.openxmlformats-officedocument.spreadsheetml.template"),
    PPT("application/vnd.ms-powerpoint"),
    POT("application/vnd.ms-powerpoint"),
    PPS("application/vnd.ms-powerpoint"),
    PPA("application/vnd.ms-powerpoint"),
    PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation"),
    POTX("application/vnd.openxmlformats-officedocument.presentationml.template"),
    PPSX("application/vnd.openxmlformats-officedocument.presentationml.slideshow"),
// Open Office
    ODT("application/vnd.oasis.opendocument.text"),
    OTT("application/vnd.oasis.opendocument.text-template"),
    OTH("application/vnd.oasis.opendocument.text-web"),
    ODM("application/vnd.oasis.opendocument.text-master"),
    ODG("application/vnd.oasis.opendocument.graphics"),
    OTG("application/vnd.oasis.opendocument.graphics-template"),
    ODP("application/vnd.oasis.opendocument.presentation"),
    OTP("application/vnd.oasis.opendocument.presentation-template"),
    ODS("application/vnd.oasis.opendocument.spreadsheet"),
    OTS("application/vnd.oasis.opendocument.spreadsheet-template"),
    ODC("application/vnd.oasis.opendocument.chart"),
    ODF("application/vnd.oasis.opendocument.formula"),
    ODB("application/vnd.oasis.opendocument.database"),
    ODI("application/vnd.oasis.opendocument.image"),
    OXT("application/vnd.openofficeorg.extension");

    private final String value;

    FileMimeTypeEnum(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static FileMimeTypeEnum fromValue(String value) {
        for (FileMimeTypeEnum b : FileMimeTypeEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }

    public static String getContentTypeByFileName(String fileName) {
        FileNameMap mimeTypes = URLConnection.getFileNameMap();
        String contentType = mimeTypes.getContentTypeFor(fileName);
        if (!StringUtils.hasText(contentType)) {
            String extension = FilenameUtils.getExtension(fileName);
            contentType = FileMimeTypeEnum.valueOf(extension.toUpperCase()).toString();
        }
        return contentType;
    }
}
