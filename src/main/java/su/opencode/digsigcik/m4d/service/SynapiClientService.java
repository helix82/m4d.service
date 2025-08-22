package su.opencode.digsigcik.m4d.service;

import org.springframework.core.io.ByteArrayResource;
import su.opencode.digsigcik.m4d.dto.NotificationCreateRequestDto;
import su.opencode.digsigcik.m4d.dto.response.NotificationResponseDto;

public interface SynapiClientService {
    NotificationResponseDto sendNotification(NotificationCreateRequestDto requestDto);

    ByteArrayResource getPartContainer(String tempFileContainer, String selector, String bearerToken);
}
