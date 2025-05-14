package su.opencode.digsigcik.m4d.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import su.opencode.digsigcik.dtos.cryptoproxy.sign.CAdESSignEvent;
import su.opencode.digsigcik.m4d.exception.BadRequestException;
import su.opencode.digsigcik.m4d.service.CryptoProxyClientService;
import su.opencode.digsigcik.m4d.service.RestTemplateService;

import java.util.Optional;

import static su.opencode.digsigcik.m4d.util.Constants.CRYPTO_PROXY_EXCEPTION;

@Slf4j
@Service
@RequiredArgsConstructor
public class CryptoProxyClientServiceImpl implements CryptoProxyClientService {

    @Value("${cryptopro.proxy-service}")
    private String CRYPTO_PROXY_SIGN_URL;

    private final RestTemplateService restTemplateService;

    @Override
    public String requestFileCadesSignature(String bearerToken, CAdESSignEvent event) {

        try {
            ResponseEntity<String> response = restTemplateService.fetchServiceResponse(
                    CRYPTO_PROXY_SIGN_URL,
                    String.class,
                    bearerToken,
                    HttpMethod.POST,
                    event
            );

            return Optional.of(response)
                    .filter(ResponseEntity::hasBody)
                    .map(ResponseEntity::getBody)
                    .orElseThrow(() -> new BadRequestException(CRYPTO_PROXY_EXCEPTION));
        } catch (HttpClientErrorException ex) {
            throw new BadRequestException(getCryptoProxyErrorMessage(ex));
        }
    }

    private String getCryptoProxyErrorMessage(HttpClientErrorException ex) {
        String responseBody = ex.getResponseBodyAsString();

        log.error("Ошибка при подписании документа облачной подписью: {}", responseBody, ex);

        if (responseBody.contains("invalid_grant")) {
            return "Вы не зарегистрированы в КриптоПро Ключ";
        } else if (responseBody.contains("invalid_pin")) {
            return "Неверный пин-код";
        } else if (responseBody.contains("certificate_not_found_id")) {
            return "Сертификат не был найден в КриптоПро Ключ";
        }

        return CRYPTO_PROXY_EXCEPTION;
    }
}
