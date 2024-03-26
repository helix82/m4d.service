package su.opencode.digsigcik.synapi.middleware;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import su.opencode.digsigcik.synapi.config.WebConfigProperties;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(Integer.MIN_VALUE)
@RequiredArgsConstructor
public class ApiCorsFilter implements Filter {

    private final WebConfigProperties webConfigProperties;

    // After change, verify that it matches
    // su.opencode.digsigcik.synapi.config.SecurityConfig.corsConfigurationSource
    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;
        if (!request.getRequestURI().startsWith("/api/")) {
            chain.doFilter(req, res);
            return;
        }
        WebConfigProperties.Cors cors = webConfigProperties.getCors();
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, cors.getAllowedOriginValue());
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, cors.getAllowedMethodsValue());
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, cors.getAllowedHeadersValue());
        response.setHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE, cors.getMaxAgeValue());
//        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        if (HttpMethod.OPTIONS.name().equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            chain.doFilter(req, res);
        }
    }
}
