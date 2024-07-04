package su.opencode.digsigcik.synapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class OrderDto {
    @JsonProperty("domainName")
    private String domainName;
    @JsonProperty("notAfter")
    private String notAfter;
    @JsonProperty("provider")
    private Long provider;
    @JsonProperty("data")
    private Map<String, String[]> data;

    public String getDomainName() {
        return domainName;
    }

    public String getNotAfter() {
        return notAfter;
    }

    public Long getProvider() {
        return provider;
    }

    public Map<String, String[]> getData() {
        return data;
    }
}

