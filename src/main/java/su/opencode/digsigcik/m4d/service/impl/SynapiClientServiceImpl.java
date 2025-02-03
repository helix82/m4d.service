package su.opencode.digsigcik.m4d.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import su.opencode.digsigcik.m4d.dto.ReportDto;
import su.opencode.digsigcik.m4d.enums.HashAlgorithm;
import su.opencode.digsigcik.m4d.exception.BadRequestException;
import su.opencode.digsigcik.m4d.service.RestTemplateService;
import su.opencode.digsigcik.m4d.service.SynapiClientService;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SynapiClientServiceImpl implements SynapiClientService {
    @Value("${synapi.container.get-part.uri}")
    private String CONTAINER_PART_URI;
    @Value("${synapi.container.get-part-json.uri}")
    private String CONTAINER_PART_JSON_URI;
    @Value("${synapi.container.create.uri}")
    private String CONTAINER_CREATE_URI;
    private final RestTemplateService restTemplateService;

    @Override
    public ByteArrayResource createContainer(String tempFileSource, String tempFileSig, HashAlgorithm hashAlgorithm, String bearerToken) {
        log.info("Отправка запроса на создание контейнера");
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("srcFile", new FileSystemResource(tempFileSource));
        map.add("sigFile", new FileSystemResource(tempFileSig));
        map.add("hashAlgorithm", hashAlgorithm);
        try {
            ResponseEntity<ByteArrayResource> responseEntity =
                    restTemplateService.fetchServiceResponse(
                            CONTAINER_CREATE_URI,
                            ByteArrayResource.class,
                            bearerToken,
                            HttpMethod.POST,
                            map
                    );

            return Optional.of(responseEntity)
                    .filter(ResponseEntity::hasBody)
                    .map(ResponseEntity::getBody)
                    .orElseThrow(() -> new BadRequestException("Ошибка создания контейнера"));

        } catch (HttpClientErrorException ex) {
            throw new BadRequestException("Ошибка создания контейнера");
        }
    }

    @Override
    public ReportDto getReportContainer(String tempFileContainer, boolean reverify, String bearerToken) {
        log.info("Отправка запроса на получение отчёта из контейнера");
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("container", new FileSystemResource(tempFileContainer));
        map.add("reverify", true);
        try {
            ResponseEntity<ReportDto> responseEntity =
                    restTemplateService.fetchServiceResponse(
                            CONTAINER_PART_JSON_URI,
                            ReportDto.class,
                            bearerToken,
                            HttpMethod.POST,
                            map
                    );

            return Optional.of(responseEntity)
                    .filter(ResponseEntity::hasBody)
                    .map(ResponseEntity::getBody)
                    .orElseThrow(() -> new BadRequestException("Ошибка получения отчёта"));

        } catch (HttpClientErrorException ex) {
            throw new BadRequestException("Ошибка получения отчёта");
        }
    }

    @Override
    public ByteArrayResource getPartContainer(String tempFileContainer, String selector, String bearerToken) {
        log.info("Отправка запроса на получение данных из контейнера");
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("container", new FileSystemResource(tempFileContainer));
        map.add("selector", selector);
        try {
            ResponseEntity<ByteArrayResource> responseEntity =
                    restTemplateService.fetchServiceResponse(
                            CONTAINER_PART_URI,
                            ByteArrayResource.class,
                            bearerToken,
                            HttpMethod.POST,
                            map
                    );

            return Optional.of(responseEntity)
                    .filter(ResponseEntity::hasBody)
                    .map(ResponseEntity::getBody)
                    .orElseThrow(() -> new BadRequestException("Ошибка получения данных"));

        } catch (HttpClientErrorException ex) {
            throw new BadRequestException("Ошибка получения данных");
        }
    }
}
