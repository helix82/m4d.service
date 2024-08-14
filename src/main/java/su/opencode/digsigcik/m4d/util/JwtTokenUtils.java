package su.opencode.digsigcik.m4d.util;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

//TODO @vitalik.sahnov Временное решение. Удалить после интеграции авторизации
@Component
public class JwtTokenUtils {
    public static String getAuthenticationEmail(Authentication authentication) {
        return (String) authentication.getPrincipal();
    }
}
