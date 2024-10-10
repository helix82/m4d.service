package su.opencode.digsigcik.m4d.util;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@UtilityClass
public class JwtTokenUtils {
    public static String getAuthenticationUsername(Authentication authentication) {
        Jwt token = (Jwt) authentication.getPrincipal();
        return (String) token.getClaims().get("preferred_username");
    }

    public static String getTokenValue(Authentication authentication) {
        return getJwt(authentication).getTokenValue();
    }

    private static Jwt getJwt(Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken)) {
            throw new IllegalArgumentException("Аутентификация не является JwtAuthenticationToken");
        }

        return ((JwtAuthenticationToken) authentication).getToken();
    }
}
