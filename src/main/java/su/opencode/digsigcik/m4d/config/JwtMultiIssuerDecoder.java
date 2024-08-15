package su.opencode.digsigcik.m4d.config;

import org.springframework.security.oauth2.jwt.*;

public class JwtMultiIssuerDecoder implements JwtDecoder {
    private final NimbusJwtDecoder internalDecoder;
    private final NimbusJwtDecoder publicDecoder;

    public JwtMultiIssuerDecoder(String internalUri, String publicUri) {
        internalDecoder = JwtDecoders.fromIssuerLocation(internalUri);
        // This is not an error!!! We need to init the public decoder from the internal URI - the publicUri might not be reachable by this server!
        publicDecoder = JwtDecoders.fromIssuerLocation(internalUri);
        publicDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(publicUri));
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            return internalDecoder.decode(token);
        } catch (JwtValidationException e) {
            return publicDecoder.decode(token);
        }
    }
}
