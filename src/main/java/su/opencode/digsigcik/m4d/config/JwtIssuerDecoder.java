package su.opencode.digsigcik.m4d.config;

import org.springframework.security.oauth2.jwt.*;

public class JwtIssuerDecoder implements JwtDecoder {
    private final NimbusJwtDecoder jwtDecoder;

    public JwtIssuerDecoder(String issuerUri) {
        jwtDecoder = JwtDecoders.fromIssuerLocation(issuerUri);
    }

    @Override
    public Jwt decode(String token) throws JwtException {
            return jwtDecoder.decode(token);
    }
}
