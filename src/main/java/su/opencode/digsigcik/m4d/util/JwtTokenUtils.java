package su.opencode.digsigcik.m4d.util;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

@UtilityClass
public class JwtTokenUtils {
    public static String getAuthenticationUsername(Authentication authentication) {
        Jwt token = (Jwt) authentication.getPrincipal();
        return (String) token.getClaims().get("preferred_username");
    }
}
