package su.opencode.digsigcik.m4d.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.m4d.dto.response.PublicKeyUserCertificateResponse;
import su.opencode.digsigcik.m4d.service.RestTemplateService;
import su.opencode.digsigcik.m4d.service.UserCertificateService;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static su.opencode.digsigcik.m4d.util.JwtTokenUtils.getTokenValue;

@Service
@RequiredArgsConstructor
public class UserCertificateServiceImpl implements UserCertificateService {
    @Value("${synapi.user-certs-me.uri}")
    private String USER_CERTIFICATE_ME_URI;
    private final RestTemplateService restTemplateService;

    @Override
    public Set<String> getUserCertificateINNs(Authentication authentication) {
        ResponseEntity<PublicKeyUserCertificateResponse[]> listUserCertificates =
                restTemplateService.fetchServiceResponse(USER_CERTIFICATE_ME_URI, PublicKeyUserCertificateResponse[].class,
                        getTokenValue(authentication), HttpMethod.GET);

        return Stream.of(Objects.requireNonNull(listUserCertificates.getBody()))
                .filter(Objects::nonNull)
                .map(PublicKeyUserCertificateResponse::getInn)
                .collect(Collectors.toSet());
    }
}
