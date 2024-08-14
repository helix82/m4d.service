package su.opencode.digsigcik.m4d.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.validation.constraints.NotNull;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

@JsonPropertyOrder({
        ContainerPartResponse.JSON_PROPERTY_CONTENT_TYPE,
        ContainerPartResponse.JSON_PROPERTY_FILE_NAME,
        ContainerPartResponse.JSON_PROPERTY_DATA
})
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2024-08-07T12:38:49.264898600+04:00[Europe/Samara]", comments = "Generator version: 7.4.0")
public class ContainerPartResponse {
    public static final String JSON_PROPERTY_CONTENT_TYPE = "contentType";
    private String contentType;

    public static final String JSON_PROPERTY_FILE_NAME = "fileName";
    private String fileName;

    public static final String JSON_PROPERTY_DATA = "data";
    private byte[] data;

    public ContainerPartResponse() {
    }

    public ContainerPartResponse contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * MIME type
     * @return contentType
     **/
    //@javax.annotation.Nonnull
    @NotNull

    @JsonProperty(JSON_PROPERTY_CONTENT_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getContentType() {
        return contentType;
    }


    @JsonProperty(JSON_PROPERTY_CONTENT_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }


    public ContainerPartResponse fileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    /**
     * File name
     * @return fileName
     **/
    //@javax.annotation.Nullable

    @JsonProperty(JSON_PROPERTY_FILE_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getFileName() {
        return fileName;
    }


    @JsonProperty(JSON_PROPERTY_FILE_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


    public ContainerPartResponse data(byte[] data) {
        this.data = data;
        return this;
    }

    /**
     * Get data
     * @return data
     **/
    //@javax.annotation.Nonnull
    @NotNull

    @JsonProperty(JSON_PROPERTY_DATA)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public byte[] getData() {
        return data;
    }


    @JsonProperty(JSON_PROPERTY_DATA)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setData(byte[] data) {
        this.data = data;
    }


    /**
     * Return true if this ContainerPart object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ContainerPartResponse containerPart = (ContainerPartResponse) o;
        return Objects.equals(this.contentType, containerPart.contentType) &&
                Objects.equals(this.fileName, containerPart.fileName) &&
                Arrays.equals(this.data, containerPart.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contentType, fileName, Arrays.hashCode(data));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ContainerPart {\n");
        sb.append("    contentType: ").append(toIndentedString(contentType)).append("\n");
        sb.append("    fileName: ").append(toIndentedString(fileName)).append("\n");
        sb.append("    data: ").append(toIndentedString(data)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

    /**
     * Convert the instance into URL query string.
     *
     * @return URL query string
     */
    public String toUrlQueryString() {
        return toUrlQueryString(null);
    }

    /**
     * Convert the instance into URL query string.
     *
     * @param prefix prefix of the query string
     * @return URL query string
     */
    public String toUrlQueryString(String prefix) {
        String suffix = "";
        String containerSuffix = "";
        String containerPrefix = "";
        if (prefix == null) {
            // style=form, explode=true, e.g. /pet?name=cat&type=manx
            prefix = "";
        } else {
            // deepObject style e.g. /pet?id[name]=cat&id[type]=manx
            prefix = prefix + "[";
            suffix = "]";
            containerSuffix = "]";
            containerPrefix = "[";
        }

        StringJoiner joiner = new StringJoiner("&");

        // add `contentType` to the URL query string
        if (getContentType() != null) {
            joiner.add(String.format("%scontentType%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getContentType()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
        }

        // add `fileName` to the URL query string
        if (getFileName() != null) {
            joiner.add(String.format("%sfileName%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getFileName()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
        }

        // add `data` to the URL query string
        if (getData() != null) {
            joiner.add(String.format("%sdata%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getData()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
        }

        return joiner.toString();
    }
}
