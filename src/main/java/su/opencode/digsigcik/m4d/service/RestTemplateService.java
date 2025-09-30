package su.opencode.digsigcik.m4d.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface RestTemplateService {
    <T> ResponseEntity<T> fetchServiceResponse(
            String url,
            Class<T> responseType,
            String bearerToken,
            HttpMethod method);

    <T, R> ResponseEntity<T> fetchServiceResponse(
            String url,
            Class<T> responseType,
            String bearerToken,
            HttpMethod method,
            R requestBody);

    <T, R> ResponseEntity<List<T>> fetchServiceResponse(
            String url,
            ParameterizedTypeReference<List<T>> parameterizedTypeReference,
            String bearerToken,
            HttpMethod method,
            R requestBody);

    <T, R> ResponseEntity<T> fetchServiceResponse(
            String url,
            Class<T> responseType,
            HttpMethod method,
            R requestBody);
}
