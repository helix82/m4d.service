package su.opencode.digsigcik.m4d.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.m4d.dto.NotificationCreateRequestDto;
import su.opencode.digsigcik.m4d.dto.response.NotificationResponseDto;
import su.opencode.digsigcik.m4d.enums.NotificationTypeEnum;
import su.opencode.digsigcik.m4d.service.NotificationFacadeService;
import su.opencode.digsigcik.m4d.service.SynapiClientService;
import su.opencode.digsigcik.m4d.service.UserCertificateService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationFacadeServiceImpl implements NotificationFacadeService {
    private final SynapiClientService synapiClientService;
    private final UserCertificateService userCertificateService;
    private final ObjectMapper objectMapper;

    @Override
    public <T> List<NotificationResponseDto> sendNotificationByINN(Authentication authentication,
                                                                   String inn,
                                                                   T payload,
                                                                   NotificationTypeEnum type) {

        return userCertificateService.findLoginsByInn(authentication, inn).stream()
                .map(login -> getNotificationResponseDto(payload, login, type))
                .collect(Collectors.toList());
    }

    private <T> NotificationResponseDto getNotificationResponseDto(T payload,
                                                                   String login,
                                                                   NotificationTypeEnum type) {

        return synapiClientService.sendNotification(new NotificationCreateRequestDto(
                login,
                objectMapper.valueToTree(payload),
                type
        ));
    }
}
