package su.opencode.digsigcik.synapi.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import su.opencode.digsigcik.synapi.dto.*;
import su.opencode.digsigcik.synapi.exception.UserAuthException;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static su.opencode.digsigcik.synapi.util.Constants.TOKEN_ERROR_MESSAGE;

//TODO @vitalik.sahnov Временное решение. Удалить после интеграции авторизации
@Component
public class JwtTokenUtils {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.lifetime}")
    private Duration jwtLifetime;

    public String generateToken(UserDetails userDetails, UserDto userDto) {
        Map<String, Object> claims = new HashMap<>();
        List<String> rolesList = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        ResourceAccessDto resourceAccessDto = new ResourceAccessDto(new PdDto(new RolesDto(rolesList)));

        claims.put("resource_access", resourceAccessDto.getPdDto());
        claims.put("preferred_username", userDetails.getUsername());
        claims.put("firstname", userDto.getFirstname());
        claims.put("lastname", userDto.getLastname());
        claims.put("patronymic", userDto.getPatronymic());

        Date issuedDate = new Date();
        Date expiredDate = new Date(issuedDate.getTime() + jwtLifetime.toMillis());
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(issuedDate)
                .setExpiration(expiredDate)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public String getPayloadJwtToken(String token) {
        String[] tokenElements = token.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();
        return new String(decoder.decode(tokenElements[1]));
    }

    public JwtPayloadDto parseJwtPayload(String token) {
        String authPayloadToken = getPayloadJwtToken(token.substring(7));
        JwtPayloadDto jwtPayloadDto;
        try {
            jwtPayloadDto = new ObjectMapper().readValue(authPayloadToken.getBytes(), JwtPayloadDto.class);
        } catch (IOException e) {
            throw new UserAuthException(TOKEN_ERROR_MESSAGE, e.getCause());
        }
        return jwtPayloadDto;
    }

    public String getUsername(String token) {
        return getAllClaimsFromToken(token).get("preferred_username", String.class);
    }

    public List<String> getRoles(String token) {
        Claims claims = getAllClaimsFromToken(token);
        Map<String, Map<String, List<String>>> resourceAccess = (Map<String, Map<String, List<String>>>) claims.get("resource_access");
        if (resourceAccess != null && resourceAccess.containsKey("pd")) {
            Map<String, List<String>> pd = resourceAccess.get("pd");
            if (pd != null && pd.containsKey("roles")) {
                return pd.get("roles");
            }
        }
        return null;
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    public static String getAuthenticationEmail(Authentication authentication) {
        return (String) authentication.getPrincipal();
    }
}
