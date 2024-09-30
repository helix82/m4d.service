package su.opencode.digsigcik.m4d.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static su.opencode.digsigcik.m4d.util.OAuthUtils.generateState;
import static su.opencode.digsigcik.m4d.util.RefererExtractor.extractDomainFromReferer;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final RPDProvider rpdProvider;
    private static final String RESPONSE_TYPE = "code";
    private static final String SCOPE = "openid";
    private static final String PART_CALLBACK_URL = "/auth";

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.sendRedirect(rpdProvider.getLoginUrl(RESPONSE_TYPE,SCOPE,generateState(), extractDomainFromReferer(request)+PART_CALLBACK_URL));
    }
}
