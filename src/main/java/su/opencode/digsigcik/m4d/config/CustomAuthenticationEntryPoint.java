package su.opencode.digsigcik.m4d.config;

import com.nimbusds.jose.shaded.json.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import su.opencode.digsigcik.m4d.exception.BadRequestException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static su.opencode.digsigcik.m4d.util.OAuthUtils.generateState;
import static su.opencode.digsigcik.m4d.util.RefererExtractor.extractDomainFromReferer;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final RPDProvider rpdProvider;
    private static final String RESPONSE_TYPE = "code";
    private static final String SCOPE = "openid";
    private static final String PART_CALLBACK_URL = "/auth";

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        try {
            String redirectUrl = rpdProvider.getLoginUrl(RESPONSE_TYPE, SCOPE, generateState(),
                    extractDomainFromReferer(request) + PART_CALLBACK_URL);

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            JSONObject jsonResponse = getJsonResponse(redirectUrl);

            response.getWriter().write(jsonResponse.toJSONString());
        } catch (BadRequestException e) {
            log.error("Ошибка при извлечении домена из заголовка Referer: " + e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            String jsonResponse = String.format(
                    "{\"timestamp\": \"%s\", \"status\": %d, \"error\": \"%s\", \"message\": \"%s\", \"uri\": \"%s\"}",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    HttpStatus.BAD_REQUEST.value(),
                    HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    e.getMessage(),
                    request.getRequestURI()
            );

            response.getWriter().write(jsonResponse);
        }
    }

    private static @NotNull JSONObject getJsonResponse(String redirectUrl) {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("status", HttpStatus.UNAUTHORIZED.value());
        jsonResponse.put("message", "Authentication required");
        jsonResponse.put("redirectUrl", redirectUrl);
        return jsonResponse;
    }
}

