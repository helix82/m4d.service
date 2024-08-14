package su.opencode.digsigcik.m4d.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ContainerSelectorCode {

    SIGNCHECK("signCheck"),
    SIGNREPORT("signReport"),
    CERTDATA("certData"),
    POACHECK("poaCheck"),
    CHECKHISTORY("checkHistory"),
    SOURCE_FILE("sourceFile"),
    SOURCE_FILE_NAME("sourceFileName"),
    SOURCE_FILE_EXTENSION("sourceFileExtension"),
    SOURCE_FILE_HASH_TYPE("sourceFileHashType"),
    SOURCE_FILE_HASH("sourceFileHash"),
    SOURCE_FILE_CONTENT_TYPE("sourceFileContentType"),
    SOURCE_FILE_FULL_NAME("sourceFileFullName"),
    SOURCE_FILE_SIZE("sourceFileSize"),
    PREVIEW_PDF("previewPdf"),
    PREVIEW_PNG("previewPng"),
    DETACHEDSIGN("detachedSign"),
    ATTACHEDSIGN("attachedSign"),
    HASHTYPE("hashType"),
    HASH("hash"),
    POACONTAINER("poaContainer"),
    POADESCRIPTION("poaDescription"),
    DESCRIPTION("description"),
    USERFIRSTNAME("userFirstname"),
    USERLASTNAME("userLastname"),
    USERPATRONYMIC("userPatronymic"),
    TIMESTAMP("timestamp"),
    USERLOGIN("userLogin"),
    SIGNATUREALGORITHM("signatureAlgorithm"),
    SIGNATURE_TYPE("signatureType"),
    CONTAINER_TYPE("containerType");

    private String value;

    ContainerSelectorCode(String value) {
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
    public static ContainerSelectorCode fromValue(String value) {
        for (ContainerSelectorCode b : ContainerSelectorCode.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }

    /**
     * Convert the instance into URL query string.
     *
     * @param prefix prefix of the query string
     * @return URL query string
     */
    public String toUrlQueryString(String prefix) {
        if (prefix == null) {
            prefix = "";
        }

        return String.format("%s=%s", prefix, this.toString());
    }

}

