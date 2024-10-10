package su.opencode.digsigcik.m4d.service.impl;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import su.opencode.digsigcik.m4d.service.RestTemplateService;

@Service
@RequiredArgsConstructor
public class RestTemplateServiceImpl implements RestTemplateService {
    private final RestTemplate restTemplate;

    @Override
    public <T> ResponseEntity<T> fetchServiceResponse(String url, Class<T> responseType, String bearerToken, HttpMethod method) {
        HttpHeaders headers = createHttpHeaders(bearerToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(
                url,
                method,
                entity,
                responseType
        );
    }

    private static @NotNull HttpHeaders createHttpHeaders(String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        return headers;
    }
}
