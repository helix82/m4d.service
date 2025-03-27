package su.opencode.digsigcik.m4d.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;

public interface RestTemplateService {

    <T> ResponseEntity<T> fetchServiceResponse(String url, Class<T> responseType, String bearerToken, HttpMethod method);
    <T, R> ResponseEntity<T> fetchServiceResponse(String url, Class<T> responseType, String bearerToken, HttpMethod method, R requestBody);

    <R> ResponseEntity<HashMap<String, String>> fetchServiceResponse(String url, ParameterizedTypeReference<HashMap<String, String>> responseType, String bearerToken, HttpMethod method, R requestBody);
}
