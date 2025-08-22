package su.opencode.digsigcik.m4d.service;

import org.springframework.security.core.Authentication;
import su.opencode.digsigcik.m4d.dto.response.NotificationResponseDto;
import su.opencode.digsigcik.m4d.enums.NotificationTypeEnum;

import java.util.List;

public interface NotificationFacadeService {
    <T> List<NotificationResponseDto> sendNotificationByINN(Authentication authentication,
                                                            String inn,
                                                            T payload,
                                                            NotificationTypeEnum type);
}
