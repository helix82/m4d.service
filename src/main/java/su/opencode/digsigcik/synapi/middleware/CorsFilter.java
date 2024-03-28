package su.opencode.digsigcik.synapi.middleware;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsProcessor;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import su.opencode.digsigcik.synapi.config.SecurityConfig;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
@Order(Integer.MIN_VALUE)
public class CorsFilter extends OncePerRequestFilter {

    private final CorsProcessor processor;
    private final CorsConfigurationSource configurationSource;

    public CorsFilter(
            CorsProcessor processor,
            @Qualifier(SecurityConfig.CORS_CFG_SOURCE_BEAN)
                    CorsConfigurationSource configurationSource) {
        this.processor = processor;
        this.configurationSource = configurationSource;
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest req,
                                    @NotNull HttpServletResponse res,
                                    @NotNull FilterChain chain
    ) throws ServletException, IOException {
        if (!CorsUtils.isCorsRequest(req)) {
            chain.doFilter(req, res);
            return;
        }
        var origin = req.getHeader(HttpHeaders.ORIGIN);
        assert origin != null; // If it's null, then it's not a CORS request.
        var cfg = configurationSource.getCorsConfiguration(req);
        if (cfg == null) {
            chain.doFilter(req, res);
            return;
        }
        cfg.setAllowedOrigins(Collections.singletonList(origin));
        boolean isValid = processor.processRequest(cfg, req, res);
        if (!isValid || CorsUtils.isPreFlightRequest(req)) return;
        chain.doFilter(req, res);
    }
}
