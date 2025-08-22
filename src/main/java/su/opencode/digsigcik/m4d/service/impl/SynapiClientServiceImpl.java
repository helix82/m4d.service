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
import org.springframework.web.client.HttpServerErrorException;
import su.opencode.digsigcik.common.exceptions.exception.custom.BadRequestException;
import su.opencode.digsigcik.m4d.dto.NotificationCreateRequestDto;
import su.opencode.digsigcik.m4d.dto.response.NotificationResponseDto;
import su.opencode.digsigcik.m4d.service.RestTemplateService;
import su.opencode.digsigcik.m4d.service.SynapiClientService;

import java.util.Optional;

import static su.opencode.digsigcik.m4d.util.Constants.CONTAINER_READ_EXCEPTION;
import static su.opencode.digsigcik.m4d.util.Constants.SYNAPI_EXCEPTION_500;

@Slf4j
@Service
@RequiredArgsConstructor
public class SynapiClientServiceImpl implements SynapiClientService {
    @Value("${synapi.container.get-part.uri}")
    private String CONTAINER_PART_URI;
    @Value("${synapi.notification.send-uri}")
    private String NOTIFICATION_SEND_URI;
    private final RestTemplateService restTemplateService;

    @Override
    public NotificationResponseDto sendNotification(NotificationCreateRequestDto requestDto) {
        try {
            ResponseEntity<NotificationResponseDto> response = restTemplateService.fetchServiceResponse(
                    NOTIFICATION_SEND_URI,
                    NotificationResponseDto.class,
                    HttpMethod.POST,
                    requestDto
            );

            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Failed to send notification to user ", e);
            return null;
        } catch (HttpServerErrorException e) {
            throw new BadRequestException(SYNAPI_EXCEPTION_500, e.getResponseBodyAsString());
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
                    .orElseThrow(() -> new BadRequestException(CONTAINER_READ_EXCEPTION));

        } catch (HttpClientErrorException ex) {
            throw new BadRequestException(CONTAINER_READ_EXCEPTION);
        }
    }
}
