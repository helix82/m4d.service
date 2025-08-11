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
import su.opencode.digsigcik.common.exceptions.exception.custom.BadRequestException;
import su.opencode.digsigcik.m4d.service.RestTemplateService;
import su.opencode.digsigcik.m4d.service.SynapiClientService;

import java.util.Optional;

import static su.opencode.digsigcik.m4d.util.Constants.CONTAINER_READ_EXCEPTION;

@Slf4j
@Service
@RequiredArgsConstructor
public class SynapiClientServiceImpl implements SynapiClientService {
    @Value("${synapi.container.get-part.uri}")
    private String CONTAINER_PART_URI;
    private final RestTemplateService restTemplateService;

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
                    .orElseThrow(() -> new BadRequestException(CONTAINER_READ_EXCEPTION));

        } catch (HttpClientErrorException ex) {
            throw new BadRequestException(CONTAINER_READ_EXCEPTION);
        }
    }
}
