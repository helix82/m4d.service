package su.opencode.digsigcik.m4d.config;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class RPDProvider {
    private final RPDProperties RPDProperties;

    public String getLoginUrl(String responseType, String scope, String state, String callbackUri) {
        return UriComponentsBuilder.fromHttpUrl(RPDProperties.getAuthorizationUri())
                .queryParam("client_id", RPDProperties.getClientId())
                .queryParam("redirect_uri", callbackUri)
                .queryParam("response_type", responseType)
                .queryParam("state", state)
                .queryParam("scope", scope)
                .toUriString();
    }
}
