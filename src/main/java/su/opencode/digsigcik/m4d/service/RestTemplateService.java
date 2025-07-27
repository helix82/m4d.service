package su.opencode.digsigcik.m4d.service;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public interface RestTemplateService {
    <T> ResponseEntity<T> fetchServiceResponse(String url, Class<T> responseType, String bearerToken, HttpMethod method);
    <T, R> ResponseEntity<T> fetchServiceResponse(String url, Class<T> responseType, String bearerToken, HttpMethod method, R requestBody);
}
